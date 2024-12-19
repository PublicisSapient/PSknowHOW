/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.urlshortener.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MurmurHash3 {
    public static int hash32x86(byte[] data) {
        int length = data.length;
        int seed = 0;
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int r1 = 15;
        int r2 = 13;
        int m = 5;
        int n = 0xe6546b64;

        int hash = seed;

        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        while (buffer.remaining() >= 4) {
            int k = buffer.getInt();

            k *= c1;
            k = Integer.rotateLeft(k, r1);
            k *= c2;

            hash ^= k;
            hash = Integer.rotateLeft(hash, r2) * m + n;
        }

        int k = 0;
        switch (buffer.remaining()) {
            case 3:
                k ^= buffer.get(2) << 16;
            case 2:
                k ^= buffer.get(1) << 8;
            case 1:
                k ^= buffer.get(0);
                k *= c1;
                k = Integer.rotateLeft(k, r1);
                k *= c2;
                hash ^= k;
        }

        hash ^= length;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);

        return hash;
    }
}