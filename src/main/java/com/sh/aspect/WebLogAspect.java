package com.sh.aspect;

import com.sh.service.log.LogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.json.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.Arrays;

/**
 * 请求的日志处理
 * AOP:面向切面编程，相对于OOP面向对象编程
 * Spring的AOP的存在目的就是为了解耦。AOP可以让一组类共享相同行为。在OOP中只能
 * 通过继承类和实现接口，来使代码的耦合度增强，且类继承只能为单继承，阻碍更多行为添加到一组类上，AOP弥补了OOP的不足
 * Spring 支持AspectJ的注解式切面编程
 * 1、使用@Aspect声明是一个切面
 * 2、使用@After、@Before、@Around定义建言(advice)，可直接将拦截规则（切点）作为参数
 * 3、其中@After、@Before、@Around参数的拦截规则作为切点（PointCut）,为了使切点复用，
 * 可使@PointCut 专门定义拦截规则，然后在@After、@Before、@Around的参数中调用。
 * 4、其中符合条件的每一个被拦截处为连接点（JoinPoint）
 */


@Aspect //使用@Aspect声明切面
@Component //@Component泛指组件，当组件不好归类的时候，我们可以使用这个注解进行标注。
public class WebLogAspect {

    @Autowired
    private LogService logService;

    private static Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.sh.controller..*.*(..))") //声明切入点是controller下面所有类的方法
    public void webLog(){}


//    @Before("execution(public * com.sh.controller..*.*(..))") //如果上面不声明切入点 也可以在这里具体声明
    @Before("webLog()") //在切入点方法执行前执行
    public void doBefore(JoinPoint joinPoint){

        startTime.set(System.currentTimeMillis());

        //接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession();
        // 记录下请求内容
        LOGGER.info("URL : " + request.getRequestURL().toString());
        LOGGER.info("HTTP_METHOD : " + request.getMethod());
        LOGGER.info("IP : " + request.getRemoteAddr());
        LOGGER.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        LOGGER.info("ARGS : " + Arrays.toString(joinPoint.getArgs())); //使用joinPoint.getArgs();获取切入点方法的入参
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()") //访问切入点方法的返回值 returning是接收返回值的形参
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        LOGGER.info("RESPONSE : " + ret);
        LOGGER.info("SPEND TIME : " + (System.currentTimeMillis() - startTime.get()));
        startTime.remove();//用完之后记得清除，不然可能导致内存泄露;
    }

}
