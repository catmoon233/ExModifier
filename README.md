<!--
//
//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                           O\  =  /O
//                        ____/`---'\____
//                      .'  \\|     |//  `.
//                     /  \\|||  :  |||//  \
//                    /  _||||| -:- |||||-  \
//                    |   | \\\  -  /// |   |
//                    | \_|  ''\---/''  |   |
//                    \  .-\__  `-`  ___/-. /
//                  ___`. .'  /--.--\  `. . __
//               ."" '<  `.___\_<|>_/___.'  >'"".
//              | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//              \  \ `-.   \_ __\ /__ _/   .-` /  /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
-->
# ExModifier

### json编写
<!--#### 示例:
#### Exemple.json
```
{
  "type": "ARMOR",
    "Exemple": {
      "weight": 2,
      "isRandom":false,
      "Randomnum":3,
      "attrGethers": {
        "id1": {
          "weight":0,//此处同上文weight，随机属性时权重
          "value": 0.2,
          "uuid": "123e4567-e89b-12d3-a456-426614174001",
          "modifierName": "ExempleModifier",
          "slot": "auto",
          "autoName": false,
          "isAutoEquipmentSlot": true,
          "autoUUID": true,
          "operation":"add"
        }
      }
    }
  }
```
#### 详解:-->
{}：根标签<br>
&emsp;|_ "type"：必选，类型，可选项有:
~~~
"ALL","UNKNOWN","ATTACKABLE","ARMOR","WEAPON",
"HELMET","CHESTPLATE","LEGGINGS","BOOTS","RANGED","MISC",
"FISHING_ROD","TRIDENT","CROSSBOW","BOW",
"SHIELD","PICKAXE","AXE","SHOVEL","HOE,SWORD","TRINKET",
"HAND","OFFHAND","MAINHAND","OFFHAND_HAND"
~~~
&emsp;|_ {} id：词条id，最终id为 类型前2个字符+id<br>
&emsp;&emsp;|_ "weight"：必选，权重<br>
&emsp;&emsp;|_ [] "OnlyItems"：可选，表示本词条仅在所包含物品上生效<br>
&emsp;&emsp;&emsp;|_ []：一个或多个物品<br>
&emsp;&emsp;&emsp;&emsp;|_ item：物品命名空间id<br>
&emsp;&emsp;|_ [] "OnlyTags"：可选，表示本词条仅在所包含物品标签上生效<br>
&emsp;&emsp;&emsp;|_ []：一个或多个物品标签<br>
&emsp;&emsp;&emsp;&emsp;|_ tag：物品标签命名空间id<br>
&emsp;&emsp;|_ [] "OnlyWashItems"：可选，表示本词条仅在所包含物品上生效<br>
&emsp;&emsp;&emsp;|_ []：一个或多个物品<br>
&emsp;&emsp;&emsp;&emsp;|_ item：物品命名空间id<br>
&emsp;&emsp;|_ "isRandom"：可选，是否随机属性，为false时则"RandomNum"无效，默认为true<br>
&emsp;&emsp;|_ "RandomNum"：可选，随机属性数，默认为全部<br>
&emsp;&emsp;|_ {} "attrGethers"：必选，属性id组<br>
&emsp;&emsp;&emsp;|_ {}：一个或多个属性<br>
&emsp;&emsp;&emsp;&emsp;|_ {} id：必选，属性id<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "value"：必选，属性值<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "uuid"：可选，UUID<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "modifierName"：可选，属性名，"uuid"为空时建议本项为空，避免uuid重复<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "slot"：必选，装备槽位，为"auto"时自动匹配<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "autoName"：可选，"modifierName"为空时自动为true<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "isAutoEquipmentSlot"：可选，为true时自动匹配装备槽位<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "autoUUID"：可选，"uuid"为空时为true，本项根据"modifierName"生成uuid<br>
&emsp;&emsp;&emsp;&emsp;&emsp;|_ "operation"：必选，属性操作符，可选项有:
~~~
"add","multiply","multiply_base","multiply_total", 
"mainhand","offhand","head","chest","legs","feet"
~~~
