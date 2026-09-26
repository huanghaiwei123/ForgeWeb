package web.rpc.responce;

import java.io.Serial;
import java.io.Serializable;

public class RpcResponce implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Boolean success;
    private Object data;
    private String errorMessage;

    public RpcResponce(Boolean success, Object data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public Boolean getSuccess() { return success; }
    public Object getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
}
