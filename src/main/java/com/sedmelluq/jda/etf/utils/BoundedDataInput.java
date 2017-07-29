/*
 *     Copyright 2015-2017 Austin Keener & Michael Ritter & Florian Spieß
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

package com.sedmelluq.jda.etf.utils;

import java.io.DataInput;

/**
 * The ETF reader/writer and all their components were created by <a href="https://github.com/sedmelluq">sedmelluq</a> 
 * and only slightly modified by the JDA team. All credit goes to the original author.
 * 
 * @author sedmelluq
 */
public interface BoundedDataInput extends DataInput
{
    int remaining();
}
