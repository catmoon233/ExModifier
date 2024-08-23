# ExModdifer

### json编写
示例:
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
#### 详解:
"type":必选，类型，可选项有:
~~~
"ALL","UNKNOWN","ATTACKABLE","ARMOR","WEAPON",
"HELMET","CHESTPLATE","LEGGINGS","BOOTS","RANGED","MISC",
"FISHING_ROD","TRIDENT","CROSSBOW","BOW",
"SHIELD","PICKAXE","AXE","SHOVEL","HOE,SWORD","TRINKET",
"HAND","OFFHAND","MAINHAND","OFFHAND_HAND"
~~~
"Exemple":必选，词条id，最终id为 类型前2个字符+id
"weight":必选，权重，
"isRandom":可选，是否随机属性，为false则"Randomnum"无效，默认为true
"Randomnum":必选，随机属性数
"id1":必选，属性id
"value":必选，属性值
"uuid":可选，UUID
"modifierName":可选，属性名
"slot":必选，装备槽位，为"auto"时自动匹配
"autoName":当"modifierName"为空时必选且值填true
"isAutoEquipmentSlot":可选，为true时自动匹配装备槽位
"autoUUID":当UUID为空时必选，且填true
"operation":必选，属性操作符，可选项有:
~~~
"add","multiply","multiply_base","multiply_total", 
"mainhand","offhand","head","chest","legs","feet"
~~~