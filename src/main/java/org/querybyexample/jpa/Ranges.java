/*
 * Copyright 2013 JAXIO http://www.jaxio.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.querybyexample.jpa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Type safe {@link Range}s for commons types
 */
public class Ranges {
    public static class RangeInteger<E> extends Range<E, Integer> {
        private static final long serialVersionUID = 1L;

        public RangeInteger(SingularAttribute<E, Integer> field) {
            super(field);
        }

        public static <E> RangeInteger<E> newRangeInteger(SingularAttribute<E, Integer> field) {
            return new RangeInteger<E>(field);
        }
    }

    public static class RangeBigInteger<E> extends Range<E, BigInteger> {
        private static final long serialVersionUID = 1L;

        public RangeBigInteger(SingularAttribute<E, BigInteger> field) {
            super(field);
        }

        public static <E> RangeBigInteger<E> newRangeBigInteger(SingularAttribute<E, BigInteger> field) {
            return new RangeBigInteger<E>(field);
        }
    }

    public static class RangeLong<E> extends Range<E, Long> {
        private static final long serialVersionUID = 1L;

        public RangeLong(SingularAttribute<E, Long> field) {
            super(field);
        }

        public static <E> RangeLong<E> newRangeLong(SingularAttribute<E, Long> field) {
            return new RangeLong<E>(field);
        }
    }

    public static class RangeDouble<E> extends Range<E, Double> {
        private static final long serialVersionUID = 1L;

        public RangeDouble(SingularAttribute<E, Double> field) {
            super(field);
        }

        public static <E> RangeDouble<E> newRangeDouble(SingularAttribute<E, Double> field) {
            return new RangeDouble<E>(field);
        }
    }

    public static class RangeFloat<E> extends Range<E, Float> {
        private static final long serialVersionUID = 1L;

        public RangeFloat(SingularAttribute<E, Float> field) {
            super(field);
        }

        public static <E> RangeFloat<E> newRangeFloat(SingularAttribute<E, Float> field) {
            return new RangeFloat<E>(field);
        }
    }

    public static class RangeBigDecimal<E> extends Range<E, BigDecimal> {
        private static final long serialVersionUID = 1L;

        public RangeBigDecimal(SingularAttribute<E, BigDecimal> field) {
            super(field);
        }

        public static <E> RangeBigDecimal<E> newRangeBigDecimal(SingularAttribute<E, BigDecimal> field) {
            return new RangeBigDecimal<E>(field);
        }
    }

    public static class RangeDate<E> extends Range<E, Date> {
        private static final long serialVersionUID = 1L;

        public RangeDate(SingularAttribute<E, Date> field) {
            super(field);
        }

        public static <T> RangeDate<T> newRangeDate(SingularAttribute<T, Date> field) {
            return new RangeDate<T>(field);
        }
    }

    public static class RangeLocalDate<E> extends Range<E, LocalDate> {
        private static final long serialVersionUID = 1L;

        public RangeLocalDate(SingularAttribute<E, LocalDate> field) {
            super(field);
        }

        public static <E> RangeLocalDate<E> newRangeLocalDate(SingularAttribute<E, LocalDate> field) {
            return new RangeLocalDate<E>(field);
        }
    }

    public static class RangeLocalDateTime<E> extends Range<E, LocalDateTime> {
        private static final long serialVersionUID = 1L;

        public RangeLocalDateTime(SingularAttribute<E, LocalDateTime> field) {
            super(field);
        }

        public static <E> RangeLocalDateTime<E> newRangeLocalDateTime(SingularAttribute<E, LocalDateTime> field) {
            return new RangeLocalDateTime<E>(field);
        }
    }
}