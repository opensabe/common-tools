package io.github.opensabe.common.utils.exception;

/**
 * Author: duchaoqun
 * Date: 2021/4/1 7:48
 */
public class SignatureException extends RuntimeException{

    private static final long serialVersionUID = 6861326223386893277L;
    public static final SignatureException INSTANCE = new SignatureException();
    public SignatureException() {
        super("incorrect signature");
    }

}
