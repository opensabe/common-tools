/*
 * Copyright 2025 opensabe-tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.opensabe.common.buffer;

import java.util.List;

/**
 * 内存批次队列，需要同步等待完成
 * 用 countDownLatch 的原因是可能会有很多
 */
//@Log4j2
public abstract class BatchBufferedCountDownQueue<E extends BufferedCountDownLatchElement> extends BatchBufferedQueue<E> {
    @Override
    protected void afterBatchFinish(List<E> batch) {
        batch.forEach(BufferedCountDownLatchElement::finish);
    }

    @Override
    protected void afterBatchError(List<E> batch, Throwable throwable) {
        batch.forEach(e -> e.error(throwable));
    }

    /**
     * 目前不能在外层有事务，因为这里面的东西不会因为外层的事务回滚而回滚
     * @param e
     */
    @Override
    public void submit(E e) {
        super.submit(e);
        e.waitUntilFinish();
    }
}
