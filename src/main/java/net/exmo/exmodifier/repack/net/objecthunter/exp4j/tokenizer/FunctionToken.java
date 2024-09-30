/*
 * Copyright 2014 Frank Asseg
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
 */
package net.exmo.exmodifier.repack.net.objecthunter.exp4j.tokenizer;

import net.exmo.exmodifier.repack.net.objecthunter.exp4j.function.Function;
import net.exmo.exmodifier.repack.net.objecthunter.exp4j.tokenizer.Token;

public class FunctionToken extends net.exmo.exmodifier.repack.net.objecthunter.exp4j.tokenizer.Token {
    private final Function function;
    public FunctionToken(final Function function) {
        super(Token.TOKEN_FUNCTION);
        this.function = function;
    }

    public Function getFunction() {
        return function;
    }
}
