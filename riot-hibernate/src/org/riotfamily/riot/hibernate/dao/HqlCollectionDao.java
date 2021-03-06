/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is Riot.
 * 
 * The Initial Developer of the Original Code is
 * Neteye GmbH.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 *   Felix Gnass [fgnass at neteye dot de]
 * 
 * ***** END LICENSE BLOCK ***** */
package org.riotfamily.riot.hibernate.dao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.riotfamily.common.log.RiotLog;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.riotfamily.common.beans.PropertyUtils;
import org.riotfamily.riot.dao.CutAndPasteEnabledDao;
import org.riotfamily.riot.dao.ListParams;
import org.riotfamily.riot.dao.Order;
import org.riotfamily.riot.dao.ParentChildDao;
import org.riotfamily.riot.dao.SortableDao;


/**
 * RiotDao implementation that loads a bean and returns one of the
 * bean's properties as (filtered) collection.
 */
public class HqlCollectionDao extends AbstractHibernateRiotDao 
		implements SortableDao, ParentChildDao, CutAndPasteEnabledDao {

	private RiotLog log = RiotLog.get(HqlCollectionDao.class);
	
	private boolean polymorph = true;
        
    private String where;
    
    private Class<?> entityClass;
    
    private Class<?> parentClass;
    
    private String parentProperty;
    
    private String collectionProperty; 
    
    
    public HqlCollectionDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

    public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
	}
    
	public Class<?> getEntityClass() {
		return entityClass;
	}

	public void setPolymorph(boolean polymorph) {
        this.polymorph = polymorph;
    }
	        
    public void setWhere(String string) {
        where = string;
    }
    
	public void setParentClass(Class<?> parentClass) {
        this.parentClass = parentClass;
    }
    
    public void setCollectionProperty(String property) {
        this.collectionProperty = property;
    }
    
    public void setParentProperty(String parentProperty) {
		this.parentProperty = parentProperty;
	}
    
	public Object getParent(Object entity) {
		if (parentProperty != null) {
			return PropertyUtils.getProperty(entity, parentProperty);
		}
		StringBuffer hql = new StringBuffer();
		hql.append("select parent from ").append(parentClass.getName());
		hql.append(" parent join parent.").append(collectionProperty);
		hql.append(" child where child = :child");
		
		Query query = getSession().createQuery(hql.toString());
		query.setMaxResults(1);
		query.setParameter("child", entity);
		return query.uniqueResult();
	}
    
	@Override
	public void save(Object entity, Object parent) {
		if (parentProperty != null) {
    		PropertyUtils.setProperty(entity, parentProperty, parent);
    	}
		getCollection(parent).add(entity);
		super.save(entity, parent);
    }
    
	@Override
    public void delete(Object entity, Object parent) {
    	getCollection(parent).remove(entity);
		super.delete(entity, parent);
    }
       
    @SuppressWarnings("unchecked")
	protected Collection<Object> getCollection(Object parent) {
		return (Collection<Object>) PropertyUtils.getProperty(parent, collectionProperty);
	}
    
    protected void buildQueryString(StringBuffer hql, ListParams params) {
        boolean hasWhere = false;
        if (!polymorph) {
            hql.append(" where this.class = ");
            hql.append(getEntityClass().getName());
            hasWhere = true;
        }
        if (where != null) {
            hql.append(hasWhere ? " and " : " where ");
            hasWhere = true;
            hql.append(where);
        }
        hql.append(getOrderBy(params));
    }

	@Override
	protected List<?> listInternal(Object parent, ListParams params) {
		StringBuffer hql = new StringBuffer("select this ");
        buildQueryString(hql, params);	                    
        
        Collection<?> c = getCollection(parent);
        Query query = getSession().createFilter(c, hql.toString());     
        
        if (params.getPageSize() > 0) {
            query.setFirstResult(params.getOffset());
            query.setMaxResults(params.getPageSize());
        }
        if (params.getFilter() != null) {
            query.setProperties(params.getFilter());
        }
        if (log.isDebugEnabled()) {
            log.debug("HQL query: " + query.getQueryString());
        }
        return query.list();
	}

	public int getListSize(Object parent, ListParams params) {
        StringBuffer hql = new StringBuffer("select count(*) ");
        if (!polymorph) {
            hql.append(" where this.class = ");
            hql.append(getEntityClass().getName());
        }
        if (where != null) {
            hql.append(polymorph ? " where " : " and ");
            hql.append(where);
        }

        Collection<?> c = getCollection(parent);
        Query query = getSession().createFilter(c, hql.toString());

        if (params.getFilter() != null) {
            query.setProperties(params.getFilter());
        }
        Number size = (Number) query.uniqueResult();
        return size.intValue();
	}
	
	protected String getOrderBy(ListParams params) {
        StringBuffer sb = new StringBuffer();
        if (params.hasOrder()) {
        	sb.append(" order by");
        	Iterator<?> it = params.getOrder().iterator();
        	while (it.hasNext()) {
        		Order order = (Order) it.next(); 
        		sb.append(" this.");
        		sb.append(order.getProperty());
        		sb.append(' ');
        		sb.append(order.isAscending() ? "asc" : "desc");
        		if (it.hasNext()) {
        			sb.append(',');
        		}
        	}
        }
        return sb.toString();
    }
        
    public void addChild(Object entity, Object parent) {
    	getCollection(parent).add(entity);
    	if (parentProperty != null) {
    		PropertyUtils.setProperty(entity, parentProperty, parent);
    	}
    }
    
    public void removeChild(Object entity, Object parent) {
    	getCollection(parent).remove(entity);
    	if (parentProperty != null) {
    		PropertyUtils.setProperty(entity, parentProperty, null);
    	}
    }

}
