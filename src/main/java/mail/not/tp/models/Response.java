package mail.not.tp.models;

/**
 * Created by viacheslav on 12.10.16.
 */


//        0 — ОК,
//        1 — запрашиваемый объект не найден,
//        2 — невалидный запрос (например, не парсится json),
//        3 — некорректный запрос (семантически),
//        4 — неизвестная ошибка.
//        5 — такой юзер уже существует

public class Response {
    public enum Codes {
        OK,
        NOT_FOUND,
        INVALID_QUERY,
        INCORRECT_QUERY,
        UNKNOWN_ERROR,
        USER_ALREDY_EXIST
    }

    private final int code;
    private final Object response;

    public Response(Object response) {
        this(Codes.OK, response);
    }

    public Response(Codes errorCode) {
        this(errorCode, errorCode);
    }

    public Response(Codes code, Object response) {
        this.code = code.ordinal();
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public Object getResponse() {
        return response;
    }
}
