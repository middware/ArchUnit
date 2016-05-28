package com.tngtech.archunit.lang.conditions;

import java.util.Collection;

import com.tngtech.archunit.lang.AbstractArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;

class ContainsOnlyCondition<T> extends AbstractArchCondition<Collection<? extends T>> {
    private final AbstractArchCondition<T> condition;

    ContainsOnlyCondition(AbstractArchCondition<T> condition) {
        this.condition = condition;
    }

    @Override
    public void check(Collection<? extends T> collection, ConditionEvents events) {
        ConditionEvents subEvents = new ConditionEvents();
        for (T fieldAccess : collection) {
            condition.check(fieldAccess, subEvents);
        }
        events.add(new OnlyConditionEvent(subEvents));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{condition=" + condition + "}";
    }

    private static class OnlyConditionEvent extends ConditionEvent {
        private Collection<ConditionEvent> allowed;
        private Collection<ConditionEvent> violating;

        public OnlyConditionEvent(ConditionEvents events) {
            this(!events.containViolation(), events.getAllowed(), events.getViolating());
        }

        public OnlyConditionEvent(boolean conditionSatisfied, Collection<ConditionEvent> allowed, Collection<ConditionEvent> violating) {
            super(conditionSatisfied, joinMessages(violating));
            this.allowed = allowed;
            this.violating = violating;
        }

        @Override
        public void addInvertedTo(ConditionEvents events) {
            events.add(new OnlyConditionEvent(isViolation(), violating, allowed));
        }
    }
}