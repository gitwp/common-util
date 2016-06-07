package com.wangp.common;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class BeanUtils {
	
	  public static <T> List<T> intersect(List<T> ls, List<T> ls2) { 
		   if(ls==null || ls2 ==null){
			  return null;
		   }
		    List<T> list = new ArrayList<T>(ls.size()); 
		    list.addAll(ls); 
		    list.retainAll(ls2); 
	        return list; 
	    }

	    public static <T>  List<T>  union(List<T> ls, List<T> ls2) { 
	      if(ls == null) {
	    	  return ls2;
	      }else if(ls2 == null){
	    	  return ls;
	      }else{
	    	  List<T> list = new ArrayList<T>(ls.size()); 
			   list.addAll(ls);  
			   list.addAll(ls2); 
		       return list; 
	      }
	    }

	    public static <T> List<T>  diff(List<T> ls, List<T> ls2) { 
	    	if(ls == null){
	    		return null;
	    	}else if(ls2 == null){
	    		return ls;
	    	}else{
	    		List<T> list = new ArrayList<T>(ls.size()); 
	  		    list.addAll(ls);  
	  		    list.removeAll(ls2); 
	  	        return list; 
	    	}
	    } 
	    
	    public static <T>  List<T>  filter(List<T> ls,String filterProperty,String filterValue) { 
		      if(CollectionUtils.isEmpty(ls)){
		    	  return ls;
		      }
			  Iterator<T> it = ls.iterator();
			  while(it.hasNext()){
			  	try {
					T bean = it.next();	
					Object value = PropertyUtils.getNestedProperty(bean, filterProperty);
					if(value!=null && filterValue.equals(value)){
						it.remove();
					}
				} catch (Exception e) {
					Logger.error(BeanUtils.class, " filter exception ", e);
				}  
			  }
			  return ls;
		    }


		
		public static <T> List<T> getBeanPropertyList(final Collection beanList, String propertyname, boolean unique) {
			List<T> result = new ArrayList<T>();
			for (Object bean : beanList) {
				try {
					T pv = (T) PropertyUtils.getProperty(bean, propertyname);
					if (pv != null && (!unique || !result.contains(pv)))
						result.add(pv);
				} catch (Exception e) {
					Logger.error(BeanUtils.class, " getBeanPropertyList exception ", e);
				}
			}
			return result;
		}

        public static Map<String,Object> getBeanToMap(Object obj){
            return  getBeanToMap(obj,false);
        }

        public static Map<String,Object> getBeanToMap(Object obj,boolean nullFilter){
            if(obj == null){
                return null;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor property : propertyDescriptors) {
                    String key = property.getName();
                    if (!key.equals("class")) {
                        Method getter = property.getReadMethod();
                        Object value = getter.invoke(obj);
                        if(nullFilter && value==null){
                            continue;
                        }
                        map.put(key, value);
                    }
                }
            } catch (Exception e) {
                Logger.error(BeanUtils.class," bean covert into map error",e);
            }
            return map;
        }
		

		/**
		 * 根据property的值将beanList分组
		 * 
		 * @param beanList
		 * @param property
		 * @return
		 */
		public static Map groupBeanList(final Collection beanList, String property) {
			return groupBeanList(beanList, property, null);
		}

		/**
		 * 根据property的值将beanList分组, null作为单独一组，key 为nullKey
		 * @param beanList
		 * @param property
		 * @param nullKey
		 * @return
		 */
		public static Map groupBeanList(final Collection beanList, String property, Object nullKey) {
			Map<Object, List> result = new LinkedHashMap<Object, List>();
			for (Object bean : beanList) {
				try {
					Object keyvalue = PropertyUtils.getNestedProperty(bean, property);
					if (keyvalue == null)
						keyvalue = nullKey;
					if (keyvalue != null) {
						List tmpList = result.get(keyvalue);
						if (tmpList == null) {
							tmpList = new ArrayList();
							result.put(keyvalue, tmpList);
						}
						tmpList.add(bean);
					}
				} catch (Exception e) {
					Logger.error(BeanUtils.class, " groupBeanList exception ", e);
				}
			}
			return result;
		}
		
		public static   Map<String,BigDecimal> merge(final Map<String,BigDecimal> a,final Map<String,BigDecimal> b){
			Map<String,BigDecimal> map = new HashMap<String,BigDecimal>();
			if(a!=null && b!=null){
				List<String> aKeys = new ArrayList<String>(a.keySet());
				List<String> bKeys = new ArrayList<String>(b.keySet());
				List<String> keys = intersect(aKeys, bKeys);
				for(String key:keys){
					BigDecimal aValue = a.get(key);
					BigDecimal bValue = b.get(key);
					BigDecimal value = (aValue!=null? aValue:BigDecimal.ZERO).add(bValue!=null?bValue:BigDecimal.ZERO);   
					map.put(key, value);
				}
			}else if(a==null){
				return b;
			}else{
				return a;
			}
			return map;
		}
		
		public static <T> boolean domainEq(T source,T target){
		       boolean rv = true;
		       if(source==null && target==null){
		    	   return true;
		       }else if(source==null || target==null){
	    		   Logger.info(BeanUtils.class, String.format(" one of the value is null [source:%s,target:%s]", source, target));
	    		   return false;
		       }
		       List<String> ignores = Arrays.asList("class","bytes","empty","id","createdAt","updatedAt","createdBy","updatedBy");  
		       PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(source.getClass());
		       for(PropertyDescriptor pd:pds){
		       try {
		     	   	   if(ignores.contains(pd.getName())){ //忽略
		    	   		   continue;
		    	   	   }
			    	   Object sourceValue = PropertyUtils.getProperty(source,pd.getName());
			    	   Object targetValue = PropertyUtils.getProperty(target,pd.getName());
			    	   if(sourceValue==null && targetValue==null){
			    		   return true;
			    	   }else  if(sourceValue==null || targetValue==null){
			    		   Logger.info(BeanUtils.class, String.format(" one of the value is null [property:%s,source:%s,target:%s]", pd.getName(), sourceValue, targetValue));
			    		   return false;
			    	   }
			    	   if(isDefineWrapClass(pd.getPropertyType())){
			    		   rv =  rv  && domainEq(sourceValue,targetValue);
			    	   }else{
			    		   if(List.class.isAssignableFrom(pd.getPropertyType())){
			    			   List sourceList = ((List)sourceValue);
			    			   List targetList = ((List)targetValue);
			    			   if(sourceList.size()==targetList.size()){
			    				   for(int i=0;i<sourceList.size();i++){
			    					   rv =  rv && domainEq(sourceList.get(i),targetList.get(i));
			        			   }
			    			   }else{
					    		   Logger.info(BeanUtils.class, String.format(" list size not equals [property:%s,source:%s,target:%s]", pd.getName(), sourceList.size(), targetList.size()));
			    				   return false;
			    			   }
			    			 
			    		   }else{
			    			   boolean compareValue = sourceValue.equals(targetValue);
			    			   if(!compareValue){
					    		   Logger.warn(BeanUtils.class, String.format(" value compare not equals [property:%s,source:%s,target:%s,compareValue:%s]", pd.getName(), sourceValue, targetValue, compareValue));
			    			   }
			    			   rv =  rv  && compareValue;
			    		   }
			    	   }
		 
				} catch (Throwable e) {
					Logger.error(BeanUtils.class, " domainEq exception ", e);
				}
		       }  
		       return rv;
			}
	 
		  
	    //基本类型与包装类判断,String不是基本类型包装类
		  public static boolean isWrapClass(Class clz) { 
		        try { 
		           return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
	 	        } catch (Exception e) { 
					Logger.error(BeanUtils.class, " isWrapClass exception ", e);
		            return false; 
		        } 
		    } 
		  
	    //如何判断一个类型是Java本身的类型，还是用户自定义的类型
		  public static boolean isDefineWrapClass(Class clz) { 
			  try { 
				  return clz == null || clz.getClassLoader() != null;    
			  } catch (Exception e) { 
					Logger.error(BeanUtils.class, " isDefineWrapClass exception ", e);
				  return false; 
			  } 
		  }
		
		public static <T> Map beanListToMap(final Collection<T> beanList, String keyproperty, String valueproperty, boolean ignoreNull) {
			Map result = new HashMap();
			if(CollectionUtils.isEmpty(beanList)){
				return result;
			}
			for (Object bean : beanList) {
				try {
					Object key = PropertyUtils.getNestedProperty(bean, keyproperty);
					Object value = PropertyUtils.getNestedProperty(bean, valueproperty);
					if (key == null)
						continue;
					if (value != null)
						result.put(key, value);
					else if (!ignoreNull)
						result.put(key, value);
				} catch (Exception e) {
				}
			}
			return result;
		}


    /**
     * build simple instance by string value
     * @param value
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T build(String value,Class<T> clazz) throws Exception{
        Constructor<T> constructor = clazz.getConstructor(new Class[]{String.class});
        constructor.setAccessible(true);
		if(value==null){
			return null;
		}
        T t = constructor.newInstance(value);
        return t;
    }

 
}
