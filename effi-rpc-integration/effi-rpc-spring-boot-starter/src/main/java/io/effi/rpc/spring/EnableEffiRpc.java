package io.effi.rpc.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({EffiRpcRegister.class})
public @interface EnableEffiRpc {

    /**
     * Scan base packages include client and server.
     *
     * @return
     */
    String[] scanBasePackages() default {};

    /**
     * Scan base packages include client.
     *
     * @return
     */
    String[] clientScan() default {};

    /**
     * Scan base packages include server.
     *
     * @return
     */
    String[] serverScan() default {};

}
