package cn.edu.sdu.online.isdu.ui.design.snake.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记Fragment某个构造器为主构造器
 *
 * @author Scott Smith 2017-12-14 16:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface PrimaryConstructor {
}
