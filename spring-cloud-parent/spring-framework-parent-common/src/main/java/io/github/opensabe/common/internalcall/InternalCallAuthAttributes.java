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

package io.github.opensabe.common.internalcall;

/**
 * Request / exchange attribute keys written after a successful inbound verify.
 * Business code must not clear the verified marker.
 */
public final class InternalCallAuthAttributes {

    public static final String VERIFIED = "opensabe.internal-call.verified";
    /**
     * Set after the first inbound attempt (success or failure) so async/ERROR re-entry
     * does not re-HMAC or re-alarm.
     */
    public static final String PROCESSED = "opensabe.internal-call.processed";
    public static final String CALLER_SERVICE = "opensabe.internal-call.caller-service";
    public static final String CALLER_IP = "opensabe.internal-call.caller-ip";

    private InternalCallAuthAttributes() {
    }
}
