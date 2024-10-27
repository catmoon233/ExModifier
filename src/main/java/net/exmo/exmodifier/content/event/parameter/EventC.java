package net.exmo.exmodifier.content.event.parameter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.event.IModBusEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class EventC<T extends LivingEvent>
{
    public List<EventParameter<?>> eventParameters = new java.util.ArrayList<>();
    public Class<T> clazz;
    public T arg0;
    public EventPriority priority;
    public boolean receiveCancelled = true;
    public Class<?> LivingEntityClass;
    public EventC<T> setLivingEntityClass(Class<? extends LivingEntity> livingEntityClass) {
        LivingEntityClass = livingEntityClass;
        return this;
    }
    public Class<?> getLivingEntityClass() {
        return LivingEntityClass;
    }
    public Field entityField;
    public EventC<T> setEntityField(String string) throws NoSuchFieldException {
        this.entityField = clazz.getField(string);
        return this;
    }
    public Field getEntityField() {
        return entityField;
    }
    public Method entityMethod;
    public EventC<T> setEntityMethod(String string) throws NoSuchMethodException {
        this.entityMethod = clazz.getMethod(string);
        return this;
    }
    public boolean receiveCancelled() {
        return receiveCancelled;
    }
    public EventC<T> setReceiveCancelled(boolean receiveCancelled) {
        this.receiveCancelled = receiveCancelled;
        return this;
    }
    public static enum Type {
        PARAMETER,
        METHOD,
        FIELD
    }
    public EventC<T> setPriority(EventPriority priority) {
        this.priority = priority;
        return this;
    }
    public EventPriority getPriority() {
            if (priority == null)return EventPriority.NORMAL;
           return priority;
    }
    public static class _setval<T>{
        public Type type;
        public String name;
        public T value;

        public _setval(Type type, String name) {
            this.type = type;
            this.name = name;
        }
        public _setval(Type type, String name, T value) {
            this.type = type;
            this.name = name;
            this.value = value;
        }
    }
    public List<_setval<?>> parameters= new java.util.ArrayList<>();

    public EventC(List<EventParameter<?>> eventParameters) {
        this.eventParameters = eventParameters;
    }
    public EventC(Class<T> clazz) {
        this.clazz = clazz;
    }
    public EventC<T> setParameter(EventParameter<?> parameter){
        eventParameters.add(parameter);
        return this;
    }
    public EventC<T> addParameter(String string,Class<?> type, Object value){
        parameters.add(new _setval<>(Type.PARAMETER, string,type.cast(value)));
        return this;
    }
    public EventC<T> addParameterField(String name)  {
        parameters.add(new _setval<>(Type.FIELD, name));
        return this;
    }
    public EventC<T> addParameterMethod(String name)  {
        parameters.add(new _setval<>(Type.METHOD, name));
        return this;
    }
    public EventC<T> setParameters() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        this.eventParameters = new java.util.ArrayList<>();
        for (EventC._setval<?> _setval : parameters) {
            if (_setval.type == Type.PARAMETER) {
                setParameter(new EventParameter<>(_setval.name, _setval.value));
            }
            if (_setval.type == Type.FIELD) {
                setParameterField(_setval.name);
            }
            if (_setval.type == Type.METHOD) {
                setParameterMethod(_setval.name);
            }
        }
        return this;
    }

    public EventC<T> setParameterField(String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);

        Object fieldValue = field.get(arg0);

        Class<?> fieldType = field.getType();

        // 检查字段类型是否为 Float
        if (fieldType == Float.class) {
            fieldValue = ((Float) fieldValue).floatValue(); // 转换为基本类型 float
        }
        if (fieldType == Double.class) {
            fieldValue = ((Double) fieldValue).doubleValue(); // 转换为基本类型 double
        }
        if (fieldType == Integer.class) {
            fieldValue = ((Integer) fieldValue).intValue(); // 转换为基本类型 int
        }
        return setParameter(new EventParameter<>(name, fieldValue));

    }

    public EventC<T> setParameterMethod(String name) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = clazz.getMethod(name);
        Object invoke = method.invoke(arg0);

        if (method.getReturnType() == Boolean.class){
            invoke = ((Boolean) invoke).booleanValue();
        }else
//        if (method.getReturnType() == String.class){
//            invoke = ((String) invoke);
//        }
        if (method.getReturnType() == Double.class){
            invoke = ((Double) invoke).doubleValue();
        }else
        if (method.getReturnType() == Integer.class) {
            invoke = ((Integer) invoke).intValue();
        }
//        else {
//            invoke = method.getReturnType().cast(invoke);
//        }
//        if (method.getReturnType().getSuperclass() == LivingEntity.class){
//            invoke
//        }
         EventC<T> tEventC = setParameter(new EventParameter<>(name, (invoke)));
        return tEventC;
    }
}
