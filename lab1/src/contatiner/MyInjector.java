package contatiner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class MyInjector {
    Map<Class, Object> MyMap = new IdentityHashMap<>();
    public void CreateContainer(String packageName){

        Set<Class> classList = findAllClassesUsingClassLoader(packageName);

        for(Class c : classList){
            try {
                Object annotation = c.getAnnotation(MyBean.class);
                if(annotation != null){
                    Object obj = c.getDeclaredConstructor().newInstance();
                    MyMap.put(c, obj);
                }
            } catch(Exception e) {

            }
        }
    }

    public Object getBean(Class clazz) throws BeanNotFoundException, IllegalAccessException {
        if(MyMap.containsKey(clazz)){
            Object obj = MyMap.get(clazz);
            List<Field> fields = Arrays.stream(obj.getClass().getDeclaredFields()).toList();

            for(Field field : fields){
                MyAutoWired annotation = field.getAnnotation(MyAutoWired.class);
                if(annotation != null){
                    if(!field.canAccess(obj))
                        field.setAccessible(true);
                    Class csType = field.getType();
                    field.set(obj, MyMap.get(csType));
                }
            }

            return obj;
        } else {
            throw new BeanNotFoundException();
        }

    }

    public Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
