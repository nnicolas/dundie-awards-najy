package com.ninjaone.dundie_awards.exception;

public class FailedToGiveAwardsException extends RuntimeException {

    public FailedToGiveAwardsException(Exception e) {
        super(e);
    }
}
