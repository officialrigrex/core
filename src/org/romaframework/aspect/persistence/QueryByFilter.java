/*
 * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
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

package org.romaframework.aspect.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.romaframework.aspect.persistence.QueryByFilterProjection.ProjectionOperator;

public class QueryByFilter extends Query {
	private Class<?>											candidateClass;

	private List<QueryByFilterItem>				items;
	private List<QueryByFilterOrder>			orders;
	private List<QueryByFilterProjection>	projections;
	private String												predicateOperator;

	public static final QueryOperator			FIELD_LIKE					= QueryOperator.LIKE;
	public static final QueryOperator			FIELD_NOT_EQUALS		= QueryOperator.NOT_EQUALS;
	public static final QueryOperator			FIELD_MAJOR_EQUALS	= QueryOperator.MAJOR_EQUALS;
	public static final QueryOperator			FIELD_MINOR_EQUALS	= QueryOperator.MINOR_EQUALS;
	public static final QueryOperator			FIELD_MAJOR					= QueryOperator.MAJOR;
	public static final QueryOperator			FIELD_MINOR					= QueryOperator.MINOR;
	public static final QueryOperator			FIELD_EQUALS				= QueryOperator.EQUALS;
	public static final QueryOperator			FIELD_CONTAINS			= QueryOperator.CONTAINS;
	public static final QueryOperator			FIELD_IN						= QueryOperator.IN;
	public static final QueryOperator			FIELD_NOT_IN				= QueryOperator.NOT_IN;

	public static final String						PREDICATE_AND				= "and";
	public static final String						PREDICATE_OR				= "or";
	public static final String						PREDICATE_NOT				= "not";

	public static final String						ORDER_ASC						= "ASC";
	public static final String						ORDER_DESC					= "DESC";

	public QueryByFilter(Class<?> iCandidateClass) {
		this(iCandidateClass, PREDICATE_AND);
	}

	public QueryByFilter(Class<?> iCandidateClass, String iPredicateOperator) {
		candidateClass = iCandidateClass;
		items = new ArrayList<QueryByFilterItem>();
		orders = new ArrayList<QueryByFilterOrder>();
		projections = new ArrayList<QueryByFilterProjection>();
		predicateOperator = iPredicateOperator;
		rangeFrom = -1;
		rangeTo = -1;
	}

	public QueryByFilter(Class<?> iCandidateClass, String iPredicateOperator, byte iStrategy, String iMode) {
		this(iCandidateClass, iPredicateOperator);
		setStrategy(iStrategy);
		setMode(iMode);
	}

	public Class<?> getCandidateClass() {
		return candidateClass;
	}

	public void addItem(String iName, QueryOperator iOperator, Object iValue) {
		addItem(new QueryByFilterItemPredicate(iName, iOperator, iValue));
	}

	public void addReverseItem(QueryByFilter byFilter, String field) {
		addReverseItem(byFilter, field, QueryOperator.EQUALS);
	}

	public void addReverseItem(QueryByFilter byFilter, String field, QueryOperator operator) {
		addItem(new QueryByFilterItemReverse(byFilter, field, operator));
	}

	public void addItem(String iCondition) {
		addItem(new QueryByFilterItemText(iCondition));
	}

	public void addProjection(String field) {
		projections.add(new QueryByFilterProjection(field));
	}

	public void addProjection(String field, ProjectionOperator operator) {
		projections.add(new QueryByFilterProjection(field, operator));
	}

	public QueryByFilterItemGroup addGroup(String predicate) {
		QueryByFilterItemGroup item = new QueryByFilterItemGroup(predicate);
		addItem(item);
		return item;
	}

	public void addItem(QueryByFilterItem item) {
		items.add(item);
	}

	public QueryByFilterItem getItem(int iPosition) {
		return items.get(iPosition);
	}

	public Iterator<QueryByFilterItem> getItemsIterator() {
		return items.iterator();
	}

	public String getPredicateOperator() {
		return predicateOperator;
	}

	public void addOrder(String iName) {
		addOrder(new QueryByFilterOrder(iName, ORDER_ASC));
	}

	public void addOrder(String iName, String iOrder) {
		addOrder(new QueryByFilterOrder(iName, iOrder));
	}

	public void addOrder(QueryByFilterOrder iOrder) {
		orders.add(iOrder);
	}

	public Iterator<QueryByFilterOrder> getOrdersIterator() {
		return orders.iterator();
	}

	public void clear() {
		items.clear();
		orders.clear();
	}

	public List<QueryByFilterItem> getItems() {
		return items;
	}

	public void setItems(List<QueryByFilterItem> items) {
		this.items = items;
	}

	public void merge(QueryByFilter source) {
		if (!candidateClass.equals(source.candidateClass)) {
			throw new RuntimeException("Cannot merge queries: expected candidate class " + candidateClass + ", found " + source.getCandidateClass());
		}
		if (!predicateOperator.equals(source.predicateOperator)) {
			QueryByFilterItemGroup group = new QueryByFilterItemGroup(source.predicateOperator);
			for (QueryByFilterItem item : source.getItems()) {
				group.addItem(item);
			}
			addItem(group);
		} else {
			this.items.addAll(source.getItems());
		}
		this.orders.addAll(source.orders);
		this.projections.addAll(source.projections);
	}

	public List<QueryByFilterProjection> getProjections() {
		return projections;
	}

}
