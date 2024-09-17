### Json格式
{}：根标签<br>
&emsp;|_ {} id：套装id<br>
&emsp;&emsp;|_ {}<br>
&emsp;&emsp;&emsp;|_ {} name：套装名<br>
&emsp;&emsp;&emsp;&emsp;|_ {} "effects"<br>
&emsp;&emsp;&emsp;&emsp;|&emsp;|_ {}<br>
&emsp;&emsp;&emsp;&emsp;|&emsp;&emsp;|_ {} effect<br>
&emsp;&emsp;&emsp;&emsp;|&emsp;&emsp;&emsp;|_ "level"<br>
&emsp;&emsp;&emsp;&emsp;|_ {} "attrGethers"<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ {}<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ {} EntryId <br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ "autoUUID"<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ [] "OnlyItems"：可选<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|&emsp;|_ []：一个或多个物品<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|&emsp;&emsp;|_ item：物品命名空间id<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ [] "OnlySlots" <br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|&emsp;|_ [] <br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|&emsp;&emsp;|_ slot<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ "value"<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ "uuid"：可选，uuid<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ "modifierName"<br>
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;|_ "operation"<br>