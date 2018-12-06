package cn.edu.sdu.online.isdu.ui.design.snake.config;

import android.util.AndroidRuntimeException;

public class SnakeConfigException extends AndroidRuntimeException {

        public SnakeConfigException() {
            super();
        }

        public SnakeConfigException(String name) {
            super(name);
        }

        public SnakeConfigException(String name, Throwable cause) {
            super(name, cause);
        }

        public SnakeConfigException(Exception cause) {
            super(cause);
        }
}

