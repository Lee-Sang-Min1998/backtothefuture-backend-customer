package com.backtothefuture.store.service;

import com.backtothefuture.domain.reservation.Reservation;
import com.backtothefuture.domain.reservation.ReservationProduct;
import com.backtothefuture.store.dto.response.ReservationResponseDto;
import com.backtothefuture.store.repository.CustomReservationRepository;
import com.backtothefuture.domain.reservation.repository.ReservationProductRepository;
import com.backtothefuture.domain.reservation.repository.ReservationRepository;
import com.backtothefuture.domain.member.Member;
import com.backtothefuture.domain.member.repository.MemberRepository;
import com.backtothefuture.domain.product.Product;
import com.backtothefuture.domain.product.repository.ProductRepository;
import com.backtothefuture.domain.store.Store;
import com.backtothefuture.domain.store.repository.StoreRepository;
import com.backtothefuture.security.service.UserDetailsImpl;
import com.backtothefuture.store.dto.request.ReservationRequestDto;
import com.backtothefuture.store.exception.MemberException;
import com.backtothefuture.store.exception.ReservationException;
import com.backtothefuture.store.exception.ProductException;
import com.backtothefuture.store.exception.StoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalTime;
import java.util.List;

import static com.backtothefuture.domain.common.enums.MemberErrorCode.*;
import static com.backtothefuture.domain.common.enums.ReservationErrorCode.*;
import static com.backtothefuture.domain.common.enums.ProductErrorCode.*;
import static com.backtothefuture.domain.common.enums.StoreErrorCode.NOT_FOUND_STORE_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ProductRepository productRepository;

    private final ReservationRepository reservationRepository;

    private final ReservationProductRepository reservationProductRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    private final CustomReservationRepository customReservationRepository;

    @Transactional
    public Long makeReservation(Long memberId, ReservationRequestDto dto) {

        Member member = memberRepository.findById(memberId)  // 현재 회원 조회
            .orElseThrow(() -> new MemberException(NOT_FIND_MEMBER_ID));

        Store store = storeRepository.findById(dto.storeId())  // 주문하고자 하는 가게 조회
            .orElseThrow(() -> new StoreException(NOT_FOUND_STORE_ID));

        if (!validateReservationTime(dto.reservationTime(),
            store.getEndTime())) // 영업 종료 30분 이전인지 판단
        {
            throw new ReservationException(NOT_VALID_RESERVATION_TIME);
        }

        Reservation reservation = Reservation.builder()
            .store(store)
            .member(member)
            .totalPrice(0)
            .reservationTime(dto.reservationTime())
            .build();

        reservationRepository.save(reservation);

        updateStock(dto, reservation);

        return reservation.getId();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDto> getReservation(UserDetailsImpl userDetails,
        Long reservationId) {
        //TODO: memberId 비교해서 권한 체크
        List<ReservationResponseDto> reservationResponseDto =
            customReservationRepository.getReservation(userDetails.getId(), reservationId);

        reservationResponseDto.forEach(ReservationResponseDto::calculateTotalPrice);

        return reservationResponseDto;
    }

    @Transactional
    public void cancelReservation(UserDetailsImpl userDetails, Long reservationId) {
        //TODO: memberId 비교해서 권한 체크
        List<ReservationProduct> reservationProducts = reservationProductRepository.findAllByReservation(
            reservationId);

        if (reservationProducts.size() == 0) {
            throw new ReservationException(NOT_FOUND_RESERVATION_PRODUCT);
        }

        reservationProducts.forEach((reservationProduct) -> {
            Product product = productRepository.findById(reservationProduct.getProduct().getId())
                .orElseThrow(() -> new ProductException(NOT_FOUND_PRODUCT_ID));
            product.updateStockWhenCancel(reservationProduct.getQuantity()); // 취소된 상품에 대해서 재고 증가
            reservationProductRepository.delete(reservationProduct); // 주문 상품 엔티티 삭제
        });

        reservationRepository.deleteById(reservationId);
    }

    /**
     * 1. 상품 조회 2. 재고, 주문 수량 비교 3. 재고 차감
     */
    private void updateStock(ReservationRequestDto dto, Reservation reservation) {

        dto.orderRequestItems().forEach(itemDto -> {

            Product product = productRepository.findProductWithPessimisticLockById(
                    itemDto.productId())  // 주문하고자 하는 상품 조회
                .orElseThrow(() -> new ProductException(NOT_FOUND_PRODUCT_ID));

            if (product.getStockQuantity() < itemDto.quantity())  // 재고가 부족하면 예외 처리
            {
                throw new ReservationException(NOT_ENOUGH_STOCK_QUANTITY);
            }

            int price = product.updateStockWhenReserve(
                itemDto.quantity()); // 재고 차감하고 상품에 대한 주문 금액 반환
            reservation.increaseTotalPrice(price); // 주문 금액 반영

            reservationProductRepository.save(
                ReservationProduct.builder()
                    .reservation(reservation)
                    .product(product)
                    .quantity(itemDto.quantity())
                    .build());
        });
    }

    private boolean validateReservationTime(LocalTime reservationTime, LocalTime endTime) {
        return reservationTime.plusMinutes(30).isBefore(endTime); // 예약 시간이 영업 종료 30분 이전인지 판단
    }

}
