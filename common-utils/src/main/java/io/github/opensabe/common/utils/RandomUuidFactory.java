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
/**
 *
 */
package io.github.opensabe.common.utils;

/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.security.SecureRandom;

/**
 * random UUIDs. This implementation uses the JDK's java.security.SecureRandom
 * to generate sufficiently random values for the UUIDs.
 * <br><br>
 * This class is a singleton, so it must be constructed through the static
 * getInstance() method.
 *
 * @author Dan Jemiolo (danj)
 */

public class RandomUuidFactory {
    private static final RandomUuidFactory _SINGLETON = new RandomUuidFactory();

    private static final char[] _HEX_VALUES = new char[]{'0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f'};

    //
    // Used to create all random numbers - guarantees unique strings
    // during the process lifetime
    //
    private static final SecureRandom _RNG = new SecureRandom();

    private static final int N = 16;

    /**
     * The default constructor is explicit so we can make it private and
     * require use of getInstance() for instantiation.
     *
     * @see #getInstance()
     */
    private RandomUuidFactory() {
        //
        // this is just to prevent instantiation
        //
    }

    public String createUUID() {
        return createUUID(N);
    }

    /**
     * @return A unique UUID of the form <em>uuid:<b>X</b></em>, where
     * <b>X</b> is the generated value.
     */
    public String createUUID(int i) {
        if (i < 1 || i > 128) {
            throw new RuntimeException("传入的长度参数错误，可生成长度应在1至128位之间。param i =" + i);
        }
        //
        // first get 8 random bytes...
        //
        int ii = (i + 1) / 2;
        byte[] bytes = new byte[ii];
        _RNG.nextBytes(bytes);

        StringBuffer uuid = new StringBuffer(i + 2);

        //
        // ...then we need to shift so they're valid hex (0-9, a-f)
        //
        for (int n = 0; n < ii; ++n) {

            //if (n == 4)uuid.append('-');

            //
            // shift the bits so they are proper hex
            //
            int hex = bytes[n] & 255;
            uuid.append(_HEX_VALUES[hex >> 4]);
            uuid.append(_HEX_VALUES[hex & 15]);
        }

        return uuid.substring(uuid.length() - i);
    }

    /**
     * @return The singleton instance of this class.
     */
    public static RandomUuidFactory getInstance() {
        return _SINGLETON;
    }

    public static void main(String[] args) {
        RandomUuidFactory factory = RandomUuidFactory.getInstance();


//		for (int j = 0; j < 100; j++) {
//			Set<String> set = new HashSet<String>();
//			System.out.println("time " + j + "##############");
//			int i = 0;
//			while (i < 3000000) {
//				i++;
//				String uuid = factory.createUUID();
//				//System.out.println(uuid);
//				if (!set.contains(uuid)) {
//					set.add(uuid);
//				} else {
//					System.out.println("break!!! number:" + set.size());
//					break;
//				}
//				if (i % 1000000 == 0) {
//					System.out.println(i);
//				}
//			}
//		}


//		int i = 0;
//		System.currentTimeMillis();
//		while (i < 100000) {
//			i++;
//			String uuid = factory.createUUID(15);
//			String b36 = Base36.encode(uuid, 12);
//			System.out.println("uuid:" + uuid + "\tbase36:" + b36);
//			
//		}
//		System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            System.out.println("uuid:" + factory.createUUID(4));
        }
    }
}
