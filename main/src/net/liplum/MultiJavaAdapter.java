package net.liplum;

import arc.util.Log;
import mindustry.Vars;
import rhino.*;
import rhino.ObjToIntMap.Iterator;
import rhino.classfile.ClassFileWriter;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("all")
public final class MultiJavaAdapter implements IdFunctionCall {
    private static final Object FTAG = "JavaAdapter";
    private static final int Id_JavaAdapter = 1;
    private static boolean inited = false;
    public static Method ensureScriptableM, findCachedFunctionM, scriptSignatureM,
        reportRuntimeError2, constructInternalM, getInterfaceAdapterCacheMap;
    public static Field membersF, ctorsF, methodsF;
    public static Class<?> JavaMembers, MemberBox;
    public static Constructor<?> IteratorCons;

    static {
        try {
            ensureScriptableM = ScriptableObject.class.getDeclaredMethod("ensureScriptable", Object.class);
            ensureScriptableM.setAccessible(true);
            membersF = NativeJavaObject.class.getDeclaredField("members");
            membersF.setAccessible(true);
            JavaMembers = Context.class.getClassLoader().loadClass("rhino.JavaMembers");
            ctorsF = JavaMembers.getDeclaredField("ctors");
            ctorsF.setAccessible(true);
            findCachedFunctionM = NativeJavaMethod.class.getDeclaredMethod("findCachedFunction", Context.class, Object[].class);
            findCachedFunctionM.setAccessible(true);
            scriptSignatureM = NativeJavaMethod.class.getDeclaredMethod("scriptSignature", Object[].class);
            scriptSignatureM.setAccessible(true);
            reportRuntimeError2 = Context.class.getDeclaredMethod("reportRuntimeError2", String.class, Object.class, Object.class);
            reportRuntimeError2.setAccessible(true);
            methodsF = NativeJavaMethod.class.getDeclaredField("methods");
            methodsF.setAccessible(true);
            MemberBox = Class.forName("rhino.MemberBox", false, NativeJavaClass.class.getClassLoader());
            constructInternalM = NativeJavaClass.class.getDeclaredMethod("constructInternal", Object[].class, MemberBox);
            constructInternalM.setAccessible(true);
            IteratorCons = Iterator.class.getDeclaredConstructor(ObjToIntMap.class);
            IteratorCons.setAccessible(true);
            getInterfaceAdapterCacheMap = ClassCache.class.getDeclaredMethod("getInterfaceAdapterCacheMap");
            getInterfaceAdapterCacheMap.setAccessible(true);
        } catch (NoSuchMethodException | NoSuchFieldException | ClassNotFoundException e) {
            Log.err(e);
        }
    }

    public MultiJavaAdapter() {
    }

    public static void init(Context cx, Scriptable scope, boolean sealed) {
        if (inited) return;
        inited = true;
        MultiJavaAdapter obj = new MultiJavaAdapter();
        IdFunctionObject ctor = new IdFunctionObject(obj, FTAG, 1, "JavaAdapter", 1, scope);
        ctor.markAsConstructor((Scriptable) null);
        if (sealed) {
            ctor.sealObject();
        }

        ctor.exportAsScopeProperty();
    }

    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (f.hasTag(FTAG) && f.methodId() == 1) {
            try {
                return js_createAdapter(cx, scope, args);
            } catch (Exception e) {
                Log.err(e);
            }
        } else {
            throw f.unknown();
        }
        return null;
    }

    public static Object convertResult(Object result, Class<?> c) {
        return result == Undefined.instance && c != ScriptRuntime.ObjectClass && c != ScriptRuntime.StringClass ? null : Context.jsToJava(result, c);
    }

    public static Scriptable createAdapterWrapper(Scriptable obj, Object adapter) {
        Scriptable scope = ScriptableObject.getTopLevelScope(obj);
        NativeJavaObject res = new NativeJavaObject(scope, adapter, (Class) null, true);
        res.setPrototype(obj);
        return res;
    }

    public static Object getAdapterSelf(Class<?> adapterClass, Object adapter) throws NoSuchFieldException, IllegalAccessException {
        Field self = adapterClass.getDeclaredField("self");
        return self.get(adapter);
    }

    static Object js_createAdapter(Context cx, Scriptable scope, Object[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        int N = args.length;
        if (N == 0) {
            throw ScriptRuntime.typeError0("msg.adapter.zero.args");
        } else {
            int classCount;
            for (classCount = 0; classCount < N - 1; ++classCount) {
                Object arg = args[classCount];
                if (arg instanceof NativeObject) {
                    break;
                }

                if (!(arg instanceof NativeJavaClass) && arg instanceof NativeJavaObject && ((NativeJavaObject) arg).unwrap() instanceof Class) {
                    args[classCount] = arg = new NativeJavaClass(scope, (Class) ((NativeJavaObject) arg).unwrap());
                }

                if (!(arg instanceof NativeJavaClass)) {
                    // throw ScriptRuntime.typeError2("msg.not.java.class.arg", String.valueOf(classCount), ScriptRuntime.toString(arg));
                }
            }

            Class<?> superClass = null;
            Class<?>[] intfs = new Class[classCount];
            int interfaceCount = 0;

            for (int i = 0; i < classCount; ++i) {
                Class<?> c = ((NativeJavaClass) args[i]).getClassObject();
                if (!c.isInterface()) {
                    if (superClass != null) {
                        throw ScriptRuntime.typeError2("msg.only.one.super", superClass.getName(), c.getName());
                    }

                    superClass = c;
                } else {
                    intfs[interfaceCount++] = c;
                }
            }

            if (superClass == null) {
                superClass = ScriptRuntime.ObjectClass;
            }

            Class<?>[] interfaces = new Class[interfaceCount];
            System.arraycopy(intfs, 0, interfaces, 0, interfaceCount);

            Scriptable obj = (Scriptable) ensureScriptableM.invoke(null, args[classCount]);
            Class<?> adapterClass = getAdapterClass(scope, superClass, interfaces, obj);
            int argsCount = N - classCount - 1;

            try {
                Object adapter;
                if (argsCount > 0) {
                    Object[] ctorArgs = new Object[argsCount + 2];
                    ctorArgs[0] = obj;
                    ctorArgs[1] = cx.getFactory();
                    System.arraycopy(args, classCount + 1, ctorArgs, 2, argsCount);
                    NativeJavaClass classWrapper = new NativeJavaClass(scope, adapterClass, true);

                    NativeJavaMethod ctors = (NativeJavaMethod) ctorsF.get(membersF.get(classWrapper));

                    int index = (int) findCachedFunctionM.invoke(ctors, cx, ctorArgs);
                    if (index < 0) {
                        String sig = (String) scriptSignatureM.invoke(null, args);
                        throw (EvaluatorException) reportRuntimeError2.invoke(null, "msg.no.java.ctor", adapterClass.getName(), sig);
                    }


                    adapter = constructInternalM.invoke(null, ctorArgs, Array.get(methodsF.get(ctors), index));
                } else {
                    Class<?>[] ctorParms = new Class[]{ScriptRuntime.ScriptableClass, ScriptRuntime.ContextFactoryClass};
                    Object[] ctorArgs = new Object[]{obj, cx.getFactory()};
                    adapter = adapterClass.getConstructor(ctorParms).newInstance(ctorArgs);
                }

                Object self = getAdapterSelf(adapterClass, adapter);
                if (self instanceof Wrapper) {
                    Object unwrapped = ((Wrapper) self).unwrap();
                    if (unwrapped instanceof Scriptable) {
                        if (unwrapped instanceof ScriptableObject) {
                            ScriptRuntime.setObjectProtoAndParent((ScriptableObject) unwrapped, scope);
                        }

                        return unwrapped;
                    }
                }

                return self;
            } catch (Exception var18) {
                throw Context.throwAsScriptRuntimeEx(var18);
            }
        }
    }

    private static ObjToIntMap getObjectFunctionNames(Scriptable obj) {
        Object[] ids = ScriptableObject.getPropertyIds(obj);
        ObjToIntMap map = new ObjToIntMap(ids.length);

        for (int i = 0; i != ids.length; ++i) {
            if (ids[i] instanceof String) {
                String id = (String) ids[i];
                Object value = ScriptableObject.getProperty(obj, id);
                if (value instanceof Function) {
                    Function f = (Function) value;
                    int length = ScriptRuntime.toInt32(ScriptableObject.getProperty(f, "length"));
                    if (length < 0) {
                        length = 0;
                    }

                    map.put(id, length);
                }
            }
        }

        return map;
    }

    private static Class<?> getAdapterClass(Scriptable scope, Class<?> superClass, Class<?>[] interfaces, Scriptable obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        ClassCache cache = ClassCache.get(scope);
        Map<MultiJavaAdapter.JavaAdapterSignature, Class<?>> generated = (Map<JavaAdapterSignature, Class<?>>) getInterfaceAdapterCacheMap.invoke(cache);
        ObjToIntMap names = getObjectFunctionNames(obj);
        MultiJavaAdapter.JavaAdapterSignature sig = new MultiJavaAdapter.JavaAdapterSignature(superClass, interfaces, names);
        Class<?> adapterClass = (Class) generated.get(sig);
        if (adapterClass == null) {
            String adapterName = "adapter" + cache.newClassSerialNumber();
            byte[] code = createAdapterCode(names, adapterName, superClass, interfaces, (String) null);
            adapterClass = loadAdapterClass(adapterName, code);
            if (cache.isCachingEnabled()) {
                generated.put(sig, adapterClass);
            }
        }

        return adapterClass;
    }

    public static byte[] createAdapterCode(ObjToIntMap functionNames, String adapterName, Class<?> superClass, Class<?>[] interfaces, String scriptClassName) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        ClassFileWriter cfw = new ClassFileWriter(adapterName, superClass.getName(), "<adapter>");
        cfw.addField("factory", "Lrhino/ContextFactory;", (short) 17);
        cfw.addField("delegee", "Lrhino/Scriptable;", (short) 17);
        cfw.addField("self", "Lrhino/Scriptable;", (short) 17);
        int interfacesCount = interfaces == null ? 0 : interfaces.length;

        for (int i = 0; i < interfacesCount; ++i) {
            if (interfaces[i] != null) {
                cfw.addInterface(interfaces[i].getName());
            }
        }

        String superName = superClass.getName().replace('.', '/');
        Constructor<?>[] ctors = superClass.getDeclaredConstructors();
        Constructor[] var9 = ctors;
        int var10 = ctors.length;

        int i;
        int mod;
        for (i = 0; i < var10; ++i) {
            Constructor<?> ctor = var9[i];
            mod = ctor.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) {
                generateCtor(cfw, adapterName, superName, ctor);
            }
        }

        generateSerialCtor(cfw, adapterName, superName);
        if (scriptClassName != null) {
            generateEmptyCtor(cfw, adapterName, superName, scriptClassName);
        }

        ObjToIntMap generatedOverrides = new ObjToIntMap();
        ObjToIntMap generatedMethods = new ObjToIntMap();

        int length;
        String methodName;
        Class[] argTypes;
        String methodSignature;
        String methodKey;
        Method[] methods;
        for (i = 0; i < interfacesCount; ++i) {
            methods = interfaces[i].getMethods();
            Method[] var29 = methods;
            length = methods.length;

            for (int var15 = 0; var15 < length; ++var15) {
                Method method = var29[var15];
                int mods = method.getModifiers();
                if (!Modifier.isStatic(mods) && !Modifier.isFinal(mods) && !method.isDefault()) {
                    methodName = method.getName();
                    argTypes = method.getParameterTypes();
                    if (!functionNames.has(methodName)) {
                        try {
                            superClass.getMethod(methodName, argTypes);
                            continue;
                        } catch (NoSuchMethodException var22) {
                        }
                    }

                    methodSignature = getMethodSignature(method, argTypes);
                    methodKey = methodName + methodSignature;
                    if (!generatedOverrides.has(methodKey)) {
                        generateMethod(cfw, adapterName, methodName, argTypes, method.getReturnType(), true);
                        generatedOverrides.put(methodKey, 0);
                        generatedMethods.put(methodName, 0);
                    }
                }
            }
        }

        Method[] _methods = getOverridableMethods(superClass);
        methods = _methods;
        mod = methods.length;

        int k;
        for (length = 0; length < mod; ++length) {
            Method method = methods[length];
            k = method.getModifiers();
            boolean isAbstractMethod = Modifier.isAbstract(k);
            methodName = method.getName();
            if (isAbstractMethod || functionNames.has(methodName)) {
                argTypes = method.getParameterTypes();
                methodSignature = getMethodSignature(method, argTypes);
                methodKey = methodName + methodSignature;
                if (!generatedOverrides.has(methodKey)) {
                    generateMethod(cfw, adapterName, methodName, argTypes, method.getReturnType(), true);
                    generatedOverrides.put(methodKey, 0);
                    generatedMethods.put(methodName, 0);
                    if (!isAbstractMethod) {
                        generateSuper(cfw, adapterName, superName, methodName, methodSignature, argTypes, method.getReturnType());
                    }
                }
            }
        }


        Iterator iter = (Iterator) IteratorCons.newInstance(functionNames);
        iter.start();

        for (; !iter.done(); iter.next()) {
            String functionName = (String) iter.getKey();
            if (!generatedMethods.has(functionName)) {
                length = iter.getValue();
                Class<?>[] parms = new Class[length];

                for (k = 0; k < length; ++k) {
                    parms[k] = ScriptRuntime.ObjectClass;
                }

                generateMethod(cfw, adapterName, functionName, parms, ScriptRuntime.ObjectClass, false);
            }
        }

        return cfw.toByteArray();
    }

    static Method[] getOverridableMethods(Class<?> clazz) {
        ArrayList<Method> list = new ArrayList();
        HashSet<String> skip = new HashSet();

        Class c;
        for (c = clazz; c != null; c = c.getSuperclass()) {
            appendOverridableMethods(c, list, skip);
        }

        for (c = clazz; c != null; c = c.getSuperclass()) {
            Class[] var4 = c.getInterfaces();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Class<?> intf = var4[var6];
                appendOverridableMethods(intf, list, skip);
            }
        }

        return (Method[]) list.toArray(new Method[0]);
    }

    private static void appendOverridableMethods(Class<?> c, ArrayList<Method> list, HashSet<String> skip) {
        Method[] methods = c.getDeclaredMethods();

        for (int i = 0; i < methods.length; ++i) {
            String methodKey = methods[i].getName() + getMethodSignature(methods[i], methods[i].getParameterTypes());
            if (!skip.contains(methodKey)) {
                int mods = methods[i].getModifiers();
                if (!Modifier.isStatic(mods)) {
                    if (Modifier.isFinal(mods)) {
                        skip.add(methodKey);
                    } else if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
                        list.add(methods[i]);
                        skip.add(methodKey);
                    }
                }
            }
        }

    }

    static Class<?> loadAdapterClass(String className, byte[] classBytes) {
        Context cx = Context.getContext();
        GeneratedClassLoader loader = cx.createClassLoader(Vars.mods.mainLoader());
        Class<?> result = loader.defineClass(className, classBytes);
        loader.linkClass(result);
        return result;
    }

    public static Function getFunction(Scriptable obj, String functionName) {
        Object x = ScriptableObject.getProperty(obj, functionName);
        if (x == Scriptable.NOT_FOUND) {
            return null;
        } else if (!(x instanceof Function)) {
            throw ScriptRuntime.notFunctionError(x, functionName);
        } else {
            return (Function) x;
        }
    }

    public static Object callMethod(ContextFactory factory, Scriptable thisObj, Function f, Object[] args, long argsToWrap) {
        if (f == null) {
            return null;
        } else {
            if (factory == null) {
                factory = ContextFactory.getGlobal();
            }

            Scriptable scope = f.getParentScope();
            if (argsToWrap == 0L) {
                return Context.call(factory, f, scope, thisObj, args);
            } else {
                Context cx = Context.getCurrentContext();
                return cx != null ? doCall(cx, scope, thisObj, f, args, argsToWrap) : factory.call((cx2) -> {
                    return doCall(cx2, scope, thisObj, f, args, argsToWrap);
                });
            }
        }
    }

    private static Object doCall(Context cx, Scriptable scope, Scriptable thisObj, Function f, Object[] args, long argsToWrap) {
        for (int i = 0; i != args.length; ++i) {
            if (0L != (argsToWrap & (long) (1 << i))) {
                Object arg = args[i];
                if (!(arg instanceof Scriptable)) {
                    args[i] = cx.getWrapFactory().wrap(cx, scope, arg, (Class) null);
                }
            }
        }

        return f.call(cx, scope, thisObj, args);
    }

    public static Scriptable runScript(Script script) {
        return (Scriptable) ContextFactory.getGlobal().call((cx) -> {
            ScriptableObject global = ScriptRuntime.getGlobal(cx);
            script.exec(cx, global);
            return global;
        });
    }

    private static void generateCtor(ClassFileWriter cfw, String adapterName, String superName, Constructor<?> superCtor) {
        short locals = 3;
        Class<?>[] parameters = superCtor.getParameterTypes();
        if (parameters.length == 0) {
            cfw.startMethod("<init>", "(Lrhino/Scriptable;Lrhino/ContextFactory;)V", (short) 1);
            cfw.add(42);
            cfw.addInvoke(183, superName, "<init>", "()V");
        } else {
            StringBuilder sig = new StringBuilder("(Lrhino/Scriptable;Lrhino/ContextFactory;");
            int marker = sig.length();
            Class[] var8 = parameters;
            int var9 = parameters.length;

            int var10;
            for (var10 = 0; var10 < var9; ++var10) {
                Class<?> c = var8[var10];
                appendTypeString(sig, c);
            }

            sig.append(")V");
            cfw.startMethod("<init>", sig.toString(), (short) 1);
            cfw.add(42);
            short paramOffset = 3;
            Class[] var14 = parameters;
            var10 = parameters.length;

            for (int var15 = 0; var15 < var10; ++var15) {
                Class<?> parameter = var14[var15];
                paramOffset = (short) (paramOffset + generatePushParam(cfw, paramOffset, parameter));
            }

            locals = paramOffset;
            sig.delete(1, marker);
            cfw.addInvoke(183, superName, "<init>", sig.toString());
        }

        cfw.add(42);
        cfw.add(43);
        cfw.add(181, adapterName, "delegee", "Lrhino/Scriptable;");
        cfw.add(42);
        cfw.add(44);
        cfw.add(181, adapterName, "factory", "Lrhino/ContextFactory;");
        cfw.add(42);
        cfw.add(43);
        cfw.add(42);
        cfw.addInvoke(184, "rhino/JavaAdapter", "createAdapterWrapper", "(Lrhino/Scriptable;Ljava/lang/Object;)Lrhino/Scriptable;");
        cfw.add(181, adapterName, "self", "Lrhino/Scriptable;");
        cfw.add(177);
        cfw.stopMethod(locals);
    }

    private static void generateSerialCtor(ClassFileWriter cfw, String adapterName, String superName) {
        cfw.startMethod("<init>", "(Lrhino/ContextFactory;Lrhino/Scriptable;Lrhino/Scriptable;)V", (short) 1);
        cfw.add(42);
        cfw.addInvoke(183, superName, "<init>", "()V");
        cfw.add(42);
        cfw.add(43);
        cfw.add(181, adapterName, "factory", "Lrhino/ContextFactory;");
        cfw.add(42);
        cfw.add(44);
        cfw.add(181, adapterName, "delegee", "Lrhino/Scriptable;");
        cfw.add(42);
        cfw.add(45);
        cfw.add(181, adapterName, "self", "Lrhino/Scriptable;");
        cfw.add(177);
        cfw.stopMethod((short) 4);
    }

    private static void generateEmptyCtor(ClassFileWriter cfw, String adapterName, String superName, String scriptClassName) {
        cfw.startMethod("<init>", "()V", (short) 1);
        cfw.add(42);
        cfw.addInvoke(183, superName, "<init>", "()V");
        cfw.add(42);
        cfw.add(1);
        cfw.add(181, adapterName, "factory", "Lrhino/ContextFactory;");
        cfw.add(187, scriptClassName);
        cfw.add(89);
        cfw.addInvoke(183, scriptClassName, "<init>", "()V");
        cfw.addInvoke(184, "rhino/JavaAdapter", "runScript", "(Lrhino/Script;)Lrhino/Scriptable;");
        cfw.add(76);
        cfw.add(42);
        cfw.add(43);
        cfw.add(181, adapterName, "delegee", "Lrhino/Scriptable;");
        cfw.add(42);
        cfw.add(43);
        cfw.add(42);
        cfw.addInvoke(184, "rhino/JavaAdapter", "createAdapterWrapper", "(Lrhino/Scriptable;Ljava/lang/Object;)Lrhino/Scriptable;");
        cfw.add(181, adapterName, "self", "Lrhino/Scriptable;");
        cfw.add(177);
        cfw.stopMethod((short) 2);
    }

    static void generatePushWrappedArgs(ClassFileWriter cfw, Class<?>[] argTypes, int arrayLength) {
        cfw.addPush(arrayLength);
        cfw.add(189, "java/lang/Object");
        int paramOffset = 1;

        for (int i = 0; i != argTypes.length; ++i) {
            cfw.add(89);
            cfw.addPush(i);
            paramOffset += generateWrapArg(cfw, paramOffset, argTypes[i]);
            cfw.add(83);
        }

    }

    private static int generateWrapArg(ClassFileWriter cfw, int paramOffset, Class<?> argType) {
        int size = 1;
        if (!argType.isPrimitive()) {
            cfw.add(25, paramOffset);
        } else if (argType == Boolean.TYPE) {
            cfw.add(187, "java/lang/Boolean");
            cfw.add(89);
            cfw.add(21, paramOffset);
            cfw.addInvoke(183, "java/lang/Boolean", "<init>", "(Z)V");
        } else if (argType == Character.TYPE) {
            cfw.add(21, paramOffset);
            cfw.addInvoke(184, "java/lang/String", "valueOf", "(C)Ljava/lang/String;");
        } else {
            cfw.add(187, "java/lang/Double");
            cfw.add(89);
            String typeName = argType.getName();
            switch (typeName.charAt(0)) {
                case 'b':
                case 'i':
                case 's':
                    cfw.add(21, paramOffset);
                    cfw.add(135);
                case 'c':
                case 'e':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                default:
                    break;
                case 'd':
                    cfw.add(24, paramOffset);
                    size = 2;
                    break;
                case 'f':
                    cfw.add(23, paramOffset);
                    cfw.add(141);
                    break;
                case 'l':
                    cfw.add(22, paramOffset);
                    cfw.add(138);
                    size = 2;
            }

            cfw.addInvoke(183, "java/lang/Double", "<init>", "(D)V");
        }

        return size;
    }

    static void generateReturnResult(ClassFileWriter cfw, Class<?> retType, boolean callConvertResult) {
        if (retType == Void.TYPE) {
            cfw.add(87);
            cfw.add(177);
        } else if (retType == Boolean.TYPE) {
            cfw.addInvoke(184, "rhino/Context", "toBoolean", "(Ljava/lang/Object;)Z");
            cfw.add(172);
        } else if (retType == Character.TYPE) {
            cfw.addInvoke(184, "rhino/Context", "toString", "(Ljava/lang/Object;)Ljava/lang/String;");
            cfw.add(3);
            cfw.addInvoke(182, "java/lang/String", "charAt", "(I)C");
            cfw.add(172);
        } else {
            String retTypeStr;
            if (retType.isPrimitive()) {
                cfw.addInvoke(184, "rhino/Context", "toNumber", "(Ljava/lang/Object;)D");
                retTypeStr = retType.getName();
                switch (retTypeStr.charAt(0)) {
                    case 'b':
                    case 'i':
                    case 's':
                        cfw.add(142);
                        cfw.add(172);
                        break;
                    case 'c':
                    case 'e':
                    case 'g':
                    case 'h':
                    case 'j':
                    case 'k':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    default:
                        throw new RuntimeException("Unexpected return type " + retType.toString());
                    case 'd':
                        cfw.add(175);
                        break;
                    case 'f':
                        cfw.add(144);
                        cfw.add(174);
                        break;
                    case 'l':
                        cfw.add(143);
                        cfw.add(173);
                }
            } else {
                retTypeStr = retType.getName();
                if (callConvertResult) {
                    cfw.addLoadConstant(retTypeStr);
                    cfw.addInvoke(184, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
                    cfw.addInvoke(184, "rhino/JavaAdapter", "convertResult", "(Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;");
                }

                cfw.add(192, retTypeStr);
                cfw.add(176);
            }
        }

    }

    private static void generateMethod(ClassFileWriter cfw, String genName, String methodName, Class<?>[] parms, Class<?> returnType, boolean convertResult) {
        StringBuilder sb = new StringBuilder();
        int paramsEnd = appendMethodSignature(parms, returnType, sb);
        String methodSignature = sb.toString();
        cfw.startMethod(methodName, methodSignature, (short) 1);
        cfw.add(42);
        cfw.add(180, genName, "factory", "Lrhino/ContextFactory;");
        cfw.add(42);
        cfw.add(180, genName, "self", "Lrhino/Scriptable;");
        cfw.add(42);
        cfw.add(180, genName, "delegee", "Lrhino/Scriptable;");
        cfw.addPush(methodName);
        cfw.addInvoke(184, "rhino/JavaAdapter", "getFunction", "(Lrhino/Scriptable;Ljava/lang/String;)Lrhino/Function;");
        generatePushWrappedArgs(cfw, parms, parms.length);
        if (parms.length > 64) {
            throw new RuntimeException("JavaAdapter can not subclass methods with more then 64 arguments.");
        } else {
            long convertionMask = 0L;

            for (int i = 0; i != parms.length; ++i) {
                if (!parms[i].isPrimitive()) {
                    convertionMask |= (long) (1 << i);
                }
            }

            cfw.addPush(convertionMask);
            cfw.addInvoke(184, "rhino/JavaAdapter", "callMethod", "(Lrhino/ContextFactory;Lrhino/Scriptable;Lrhino/Function;[Ljava/lang/Object;J)Ljava/lang/Object;");
            generateReturnResult(cfw, returnType, convertResult);
            cfw.stopMethod((short) paramsEnd);
        }
    }

    private static int generatePushParam(ClassFileWriter cfw, int paramOffset, Class<?> paramType) {
        if (!paramType.isPrimitive()) {
            cfw.addALoad(paramOffset);
            return 1;
        } else {
            String typeName = paramType.getName();
            switch (typeName.charAt(0)) {
                case 'b':
                case 'c':
                case 'i':
                case 's':
                case 'z':
                    cfw.addILoad(paramOffset);
                    return 1;
                case 'd':
                    cfw.addDLoad(paramOffset);
                    return 2;
                case 'e':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                default:
                    throw Kit.codeBug();
                case 'f':
                    cfw.addFLoad(paramOffset);
                    return 1;
                case 'l':
                    cfw.addLLoad(paramOffset);
                    return 2;
            }
        }
    }

    private static void generatePopResult(ClassFileWriter cfw, Class<?> retType) {
        if (retType.isPrimitive()) {
            String typeName = retType.getName();
            switch (typeName.charAt(0)) {
                case 'b':
                case 'c':
                case 'i':
                case 's':
                case 'z':
                    cfw.add(172);
                    break;
                case 'd':
                    cfw.add(175);
                case 'e':
                case 'g':
                case 'h':
                case 'j':
                case 'k':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                default:
                    break;
                case 'f':
                    cfw.add(174);
                    break;
                case 'l':
                    cfw.add(173);
            }
        } else {
            cfw.add(176);
        }

    }

    private static void generateSuper(ClassFileWriter cfw, String genName, String superName, String methodName, String methodSignature, Class<?>[] parms, Class<?> returnType) {
        cfw.startMethod("super$" + methodName, methodSignature, (short) 1);
        cfw.add(25, 0);
        int paramOffset = 1;
        Class[] var8 = parms;
        int var9 = parms.length;

        for (int var10 = 0; var10 < var9; ++var10) {
            Class<?> parm = var8[var10];
            paramOffset += generatePushParam(cfw, paramOffset, parm);
        }

        cfw.addInvoke(183, superName, methodName, methodSignature);
        if (!returnType.equals(Void.TYPE)) {
            generatePopResult(cfw, returnType);
        } else {
            cfw.add(177);
        }

        cfw.stopMethod((short) (paramOffset + 1));
    }

    private static String getMethodSignature(Method method, Class<?>[] argTypes) {
        StringBuilder sb = new StringBuilder();
        appendMethodSignature(argTypes, method.getReturnType(), sb);
        return sb.toString();
    }

    static int appendMethodSignature(Class<?>[] argTypes, Class<?> returnType, StringBuilder sb) {
        sb.append('(');
        int firstLocal = 1 + argTypes.length;
        Class[] var4 = argTypes;
        int var5 = argTypes.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            Class<?> type = var4[var6];
            appendTypeString(sb, type);
            if (type == Long.TYPE || type == Double.TYPE) {
                ++firstLocal;
            }
        }

        sb.append(')');
        appendTypeString(sb, returnType);
        return firstLocal;
    }

    private static StringBuilder appendTypeString(StringBuilder sb, Class<?> type) {
        while (type.isArray()) {
            sb.append('[');
            type = type.getComponentType();
        }

        if (type.isPrimitive()) {
            char typeLetter;
            if (type == Boolean.TYPE) {
                typeLetter = 'Z';
            } else if (type == Long.TYPE) {
                typeLetter = 'J';
            } else {
                String typeName = type.getName();
                typeLetter = Character.toUpperCase(typeName.charAt(0));
            }

            sb.append(typeLetter);
        } else {
            sb.append('L');
            sb.append(type.getName().replace('.', '/'));
            sb.append(';');
        }

        return sb;
    }

    static int[] getArgsToConvert(Class<?>[] argTypes) {
        int count = 0;

        for (int i = 0; i != argTypes.length; ++i) {
            if (!argTypes[i].isPrimitive()) {
                ++count;
            }
        }

        if (count == 0) {
            return null;
        } else {
            int[] array = new int[count];
            count = 0;

            for (int i = 0; i != argTypes.length; ++i) {
                if (!argTypes[i].isPrimitive()) {
                    array[count++] = i;
                }
            }

            return array;
        }
    }

    static class JavaAdapterSignature {
        Class<?> superClass;
        Class<?>[] interfaces;
        ObjToIntMap names;

        JavaAdapterSignature(Class<?> superClass, Class<?>[] interfaces, ObjToIntMap names) {
            this.superClass = superClass;
            this.interfaces = interfaces;
            this.names = names;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof MultiJavaAdapter.JavaAdapterSignature)) {
                return false;
            } else {
                MultiJavaAdapter.JavaAdapterSignature sig = (MultiJavaAdapter.JavaAdapterSignature) obj;
                if (this.superClass != sig.superClass) {
                    return false;
                } else {
                    if (this.interfaces != sig.interfaces) {
                        if (this.interfaces.length != sig.interfaces.length) {
                            return false;
                        }

                        for (int i = 0; i < this.interfaces.length; ++i) {
                            if (this.interfaces[i] != sig.interfaces[i]) {
                                return false;
                            }
                        }
                    }

                    if (this.names.size() != sig.names.size()) {
                        return false;
                    } else {
                        try {
                            Iterator iter = (Iterator) IteratorCons.newInstance(this.names);
                            iter.start();


                            while (!iter.done()) {
                                String name = (String) iter.getKey();
                                int arity = iter.getValue();
                                if (arity != sig.names.get(name, arity + 1)) {
                                    return false;
                                }

                                iter.next();
                            }

                            return true;
                        } catch (Exception e) {
                            Log.err(e);
                            return false;
                        }
                    }
                }
            }
        }

        public int hashCode() {
            return this.superClass.hashCode() + Arrays.hashCode(this.interfaces) ^ this.names.size();
        }
    }
}
