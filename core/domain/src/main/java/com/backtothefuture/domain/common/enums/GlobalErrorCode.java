package com.backtothefuture.domain.common.enums;

import org.springframework.http.HttpStatus;

import com.backtothefuture.domain.response.ErrorResponse;

import lombok.Getter;

@Getter
public enum GlobalErrorCode implements BaseErrorCode {
	VALIDATION_FAILED(400,"입력값에 대한 검증에 실패했습니다.", HttpStatus.BAD_REQUEST),
	INTERNAL_SERVER_ERROR(500,"서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final int errorCode;
	private final String errorMessage;
	private final HttpStatus status;

	GlobalErrorCode(int errorCode, String message, HttpStatus status) {
		this.errorCode = errorCode;
		this.errorMessage = message;
		this.status = status;
	}

	@Override
	public ErrorResponse getErrorResponse() {
		return new ErrorResponse(this.errorCode, this.errorMessage);
	}

}