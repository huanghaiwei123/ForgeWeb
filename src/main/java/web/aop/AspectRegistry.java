package web.aop;

import lombok.Getter;

import java.util.List;

public class AspectRegistry {
    @Getter
    private List<Object> aspects;
    public AspectRegistry(List<Object> aspects) {
        this.aspects = aspects;
    }
}
