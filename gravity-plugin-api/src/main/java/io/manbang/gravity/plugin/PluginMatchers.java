package io.manbang.gravity.plugin;

import io.manbang.gravity.bytebuddy.description.method.MethodDescription;
import io.manbang.gravity.bytebuddy.description.type.TypeDescription;
import io.manbang.gravity.bytebuddy.matcher.ElementMatcher;

import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isAnnotation;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isConstructor;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isInterface;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isPublic;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.isStatic;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.namedOneOf;
import static io.manbang.gravity.bytebuddy.matcher.ElementMatchers.not;

/**
 * 通用的插件匹配器
 *
 * @author 章多亮
 */
public interface PluginMatchers {

    /**
     * 类型匹配器
     *
     * @author 章多亮
     */
    enum Types {
        /**
         * Spring Bean
         */
        SPRING_BEAN(isAnnotatedWith(namedOneOf("org.springframework.stereotype.Controller",
                "org.springframework.web.bind.annotation.RestController",
                "org.springframework.stereotype.Component",
                "org.springframework.stereotype.Repository",
                "org.springframework.stereotype.Service"))
                .and(not(isAnnotation().or(isInterface()))));

        private final ElementMatcher<TypeDescription> typeMatcher;

        Types(ElementMatcher<TypeDescription> typeMatcher) {
            this.typeMatcher = typeMatcher;
        }

        public ElementMatcher<TypeDescription> getTypeMatcher() {
            return typeMatcher;
        }
    }


    /**
     * 方法匹配器
     *
     * @author 章多亮
     */
    enum Methods {
        /**
         * Spring MVC Controller 方法
         */
        SPRING_REQUEST_MAPPING(isAnnotatedWith(namedOneOf("org.springframework.web.bind.annotation.RequestMapping",
                "org.springframework.web.bind.annotation.GetMapping",
                "org.springframework.web.bind.annotation.PostMapping",
                "org.springframework.web.bind.annotation.PutMapping",
                "org.springframework.web.bind.annotation.DeleteMapping",
                "org.springframework.web.bind.annotation.PatchMapping",
                "org.springframework.web.bind.annotation.DeleteMapping"))),
        /**
         * 公共的非静态方法
         */
        PUBLIC(isPublic().and(not(isStatic().or(isConstructor())))),
        ;


        private final ElementMatcher<MethodDescription> methodMatcher;

        Methods(ElementMatcher<MethodDescription> typeMatcher) {
            this.methodMatcher = typeMatcher;
        }

        public ElementMatcher<MethodDescription> getMethodMatcher() {
            return methodMatcher;
        }
    }


}
