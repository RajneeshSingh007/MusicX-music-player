package com.rks.musicx.misc.utils;

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.util.Base64;

import javax.annotation.Nonnull;

/**
 * Created by Coolalien on 5/28/2017.
 */
public final class Encryption {

    @Nonnull
    public static String decrypt(@Nonnull String message, @Nonnull String salt) {
        return xor(new String(Base64.decode(message, 0)), salt);
    }

    @Nonnull
    public static String encrypt(@Nonnull String message, @Nonnull String salt) {
        return new String(Base64.encode(xor(message, salt).getBytes(), 0));
    }

    /**
     * Encrypts or decrypts a base-64 string using a XOR cipher.
     */
    @Nonnull
    public static String xor(@Nonnull String message, @Nonnull String salt) {
        final char[] m = message.toCharArray();
        final int ml = m.length;

        final char[] s = salt.toCharArray();
        final int sl = s.length;

        final char[] res = new char[ml];
        for (int i = 0; i < ml; i++) {
            res[i] = (char) (m[i] ^ s[i % sl]);
        }
        return new String(res);
    }

}
