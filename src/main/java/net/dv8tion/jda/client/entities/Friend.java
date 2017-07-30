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

package net.dv8tion.jda.client.entities;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.requests.RestAction;

import javax.annotation.CheckReturnValue;
import java.time.OffsetDateTime;

public interface Friend extends Relationship
{
    Game getGame();

    OnlineStatus getOnlineStatus();

    OffsetDateTime getOnlineStatusModifiedTime();

    @CheckReturnValue
    RestAction<Void> removeFriend();

    // TODO: List<Group> getMutualGroups(); could also be in User interface...

    // TODO: Create new Group
    //GroupAction openGroup();
    // allow adding more friends in GroupAction? definitely this if name can be set in create payload
    // otherwise even:
    //RestAction<Group> openGroup(Friend... friends);
    //RestAction<Group> openGroup(Collection<Friend> friends);
}
