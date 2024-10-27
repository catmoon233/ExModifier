package net.exmo.exmodifier.content.event.parameter;

import net.minecraft.world.entity.LivingEntity;

public class EventParameter<T> {
    public EventParameter(String name, T value) {
        this.name = name;
        this.value = value;
//        if (value instanceof  LivingEntity){
//            this.type = Type.LivingEntity;
//
//        }
    }
//
//    public static enum Type {
//        LivingEntity,
//        String,
//        Double,
//        Integer,
//        Boolean
//    }
//    public EventParameter<T> ChangeType(Class<?> type){
//        if (type == String.class){
//            this.type = Type.String;
//        }else if (type == Double.class){
//            this.type = Type.Double;
//        }else if (type == Integer.class){
//            this.type = Type.Integer;
//        }else if (type == Boolean.class){
//            this.type = Type.Boolean;
//        }
//        return this;
//    };
//    public Type type;
    public String name;
    public T value;
    public T getValue(){
        return value;
    }
    public String    getUUID(){
        if (value instanceof LivingEntity e)return e.getUUID().toString();
        return (String) value;
    }
    public double getDouble() {
        if (value instanceof Double d) {
            return d;
        } else if (value instanceof Float f) {
            return f.doubleValue();
        } else {
            return 0;
        }
    }

    public int       getInt(){
        return (int) value;
    }
    public boolean   getBoolean(){
        return (boolean) value;
    }
    public LivingEntity getLivingEntity(){
        return (LivingEntity) value;
    }

    public String getKey() {
        return name;
    }
}