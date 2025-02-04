// package com.kiskee.vocabulary.aspect.logging;
//
// import com.kiskee.vocabulary.util.IdentityUtil;
// import lombok.AllArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.aspectj.lang.JoinPoint;
// import org.aspectj.lang.annotation.AfterReturning;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.data.repository.support.Repositories;
// import org.springframework.stereotype.Component;
//
// import java.util.Arrays;
//
// @Slf4j
// @Aspect
// @Component
// @AllArgsConstructor
// public class LoggingAspect {
//
//    private final Repositories repositories;
//
//    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository.save(..))")
//    public void repositorySaveOperation() {
//    }
//
//    @AfterReturning(pointcut = "repositorySaveOperation()", returning = "result")
//    public void logAfterSaving(Object result) {
//        log.info("[{}] successfully saved for user [{}]", result.getClass().getSimpleName(),
//        IdentityUtil.getUserId());
//    }
//
//    @Pointcut("execution(* com.kiskee.vocabulary.repository..*.exists*(..))")
//    public void repositoryExistsOperation() {
//    }
//
//    @AfterReturning(pointcut = "repositoryExistsOperation()", returning = "result")
//    public void logAfterExistsOperation(JoinPoint joinPoint, Object result) {
//        System.out.println(joinPoint.getSignature().getDeclaringType().getSimpleName().indexOf("Repository"));
//        System.out.println(joinPoint.getSignature().getDeclaringType().getSimpleName().substring(0, joinPoint
//        .getSignature().getDeclaringType().getSimpleName().indexOf("Repository")));
//        System.out.println((Arrays.stream(joinPoint.getSignature().getDeclaringType().getGenericInterfaces())
//        .filter(gen -> gen.getTypeName().equals("Dictionary")).findFirst()));
//        System.out.println(joinPoint.getClass());
//        System.out.println(repositories.getPersistentEntity(joinPoint.getClass()));
//        log.info("Exists [{}] operation returned [{}] for user [{}]", joinPoint.getSignature().getName(), result,
//                IdentityUtil.getUserId());
//    }
//
// }
