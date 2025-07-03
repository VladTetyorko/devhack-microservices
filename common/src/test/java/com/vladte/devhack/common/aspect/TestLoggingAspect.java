package com.vladte.devhack.common.aspect;

import io.qameta.allure.Allure;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AspectJ aspect for logging method calls and execution times during tests.
 * This aspect will log method entry and exit, parameters, return values, and execution time.
 * It will also attach this information to Allure reports.
 */
@Aspect
@Component
public class TestLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(TestLoggingAspect.class);

    /**
     * Pointcut for all service methods in the test packages.
     */
    @Pointcut("execution(* com.vladte.devhack.common.service..*.*(..))")
    public void serviceMethod() {
    }

    /**
     * Logs method entry, parameters, return value, and execution time.
     *
     * @param joinPoint the join point
     * @return the result of the method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("serviceMethod()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getDeclaringType().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // Log method entry
        String entryMessage = String.format("Entering %s.%s with parameters: %s",
                className, methodName, Arrays.toString(args));
        logger.debug(entryMessage);

        // Measure execution time
        long startTime = System.currentTimeMillis();
        Object result;

        try {
            // Execute the method
            result = joinPoint.proceed();

            // Log method exit
            long executionTime = System.currentTimeMillis() - startTime;
            String exitMessage = String.format("Exiting %s.%s (execution time: %d ms) with result: %s",
                    className, methodName, executionTime, result);
            logger.debug(exitMessage);

            // Attach to Allure report
            attachToAllureReport(className, methodName, args, result, executionTime, null);

            return result;
        } catch (Throwable e) {
            // Log exception
            long executionTime = System.currentTimeMillis() - startTime;
            String errorMessage = String.format("Exception in %s.%s (execution time: %d ms): %s",
                    className, methodName, executionTime, e.getMessage());
            logger.error(errorMessage, e);

            // Attach to Allure report
            attachToAllureReport(className, methodName, args, null, executionTime, e);

            throw e;
        }
    }

    /**
     * Attaches method execution details to the Allure report.
     *
     * @param className     the class name
     * @param methodName    the method name
     * @param args          the method arguments
     * @param result        the method result
     * @param executionTime the execution time in milliseconds
     * @param exception     the exception, if any
     */
    private void attachToAllureReport(String className, String methodName, Object[] args,
                                      Object result, long executionTime, Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method: ").append(className).append(".").append(methodName).append("\n");
        sb.append("Parameters: ").append(Arrays.toString(args)).append("\n");
        sb.append("Execution time: ").append(executionTime).append(" ms\n");

        if (exception != null) {
            sb.append("Exception: ").append(exception.getClass().getName())
                    .append(" - ").append(exception.getMessage());
        } else {
            sb.append("Result: ").append(result);
        }

        Allure.addAttachment("Method Execution: " + className + "." + methodName,
                "text/plain", sb.toString(), ".txt");
    }
}