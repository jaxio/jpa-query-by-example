/*
 * Copyright 2015 JAXIO http://www.jaxio.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaxio.jpa.querybyexample;

import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.metamodel.SingularAttribute;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.lucene.queryparser.classic.QueryParser.escape;

public class DefaultLuceneQueryBuilder implements LuceneQueryBuilder {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DefaultLuceneQueryBuilder.class);

    private static final String SPACES_OR_PUNCTUATION = "\\p{Punct}|\\p{Blank}";

    @Override
    public Query build(FullTextEntityManager fullTextEntityManager, SearchParameters searchParameters, List<SingularAttribute<?, ?>> availableProperties) {
        List<String> clauses = getAllClauses(searchParameters, searchParameters.getTerms(), availableProperties);

        StringBuilder query = new StringBuilder();
        query.append("+(");
        for (String clause : clauses) {
            if (query.length() > 2) {
                query.append(" AND ");
            }
            query.append(clause);
        }
        query.append(")");

        if (query.length() == 3) {
            return null;
        }
        log.debug("Lucene query: {}", query);
        try {
            return new QueryParser(availableProperties.get(0).getName(), fullTextEntityManager.getSearchFactory().getAnalyzer("custom"))
                    .parse(query.toString());
        } catch (Exception e) {
            throw propagate(e);
        }
    }

    private List<String> getAllClauses(SearchParameters sp, List<TermSelector> terms, List<SingularAttribute<?, ?>> availableProperties) {
        List<String> clauses = newArrayList();
        for (TermSelector term : terms) {
            if (term.isNotEmpty()) {
                String clause = getClause(sp, term.getSelected(), term.getAttribute(), term.isOrMode(), availableProperties);
                if (isNotBlank(clause)) {
                    clauses.add(clause);
                }
            }
        }
        return clauses;
    }

    private String getClause(SearchParameters sp, List<String> terms, SingularAttribute<?, ?> property, boolean orMode,
                             List<SingularAttribute<?, ?>> availableProperties) {
        if (property != null) {
            checkArgument(availableProperties.contains(property), property + " is not indexed");
            StringBuilder subQuery = new StringBuilder();
            if (terms != null) {
                subQuery.append("(");
                for (String wordWithSpacesOrPunctuation : terms) {
                    if (isBlank(wordWithSpacesOrPunctuation)) {
                        continue;
                    }
                    List<String> wordElements = newArrayList();
                    for (String str : wordWithSpacesOrPunctuation.split(SPACES_OR_PUNCTUATION)) {
                        if (isNotBlank(str)) {
                            wordElements.add(str);
                        }
                    }
                    if (!wordElements.isEmpty()) {
                        if (subQuery.length() > 1) {
                            subQuery.append(" ").append(orMode ? "OR" : "AND").append(" ");
                        }
                        subQuery.append(buildSubQuery(property, wordElements, sp));
                    }
                }
                subQuery.append(")");
            }
            if (subQuery.length() > 2) {
                return subQuery.toString();
            }
        } else {
            return getOnAnyClause(sp, terms, availableProperties, orMode, availableProperties);
        }
        return null;
    }

    private String buildSubQuery(SingularAttribute<?, ?> property, List<String> terms, SearchParameters sp) {
        StringBuilder subQuery = new StringBuilder();
        subQuery.append("(");
        for (String term : terms) {
            if (subQuery.length() > 1) {
                subQuery.append(" AND ");
            }
            if (sp.getSearchSimilarity() != null) {
                subQuery.append(property.getName() + ":" + escapeForFuzzy(lowerCase(term)) + "~" + sp.getSearchSimilarity());
            } else {
                subQuery.append(property.getName() + ":" + escape(lowerCase(term)));
            }
        }
        subQuery.append(")");
        return subQuery.toString();
    }

    private String getOnAnyClause(SearchParameters sp, List<String> terms, List<SingularAttribute<?, ?>> properties, boolean orMode,
                                  List<SingularAttribute<?, ?>> availableProperties) {
        List<String> subClauses = newArrayList();
        for (SingularAttribute<?, ?> property : properties) {
            String clause = getClause(sp, terms, property, orMode, availableProperties);
            if (isNotBlank(clause)) {
                subClauses.add(clause);
            }
        }
        if (subClauses.isEmpty()) {
            return null;
        }
        if (subClauses.size() > 1) {
            StringBuilder subQuery = new StringBuilder();
            subQuery.append("(");
            for (String subClause : subClauses) {
                if (subQuery.length() > 1) {
                    subQuery.append(" OR ");
                }
                subQuery.append(subClause);
            }
            subQuery.append(")");
            return subQuery.toString();
        } else {
            return subClauses.get(0);
        }
    }

    /**
     * Apply same filtering as "custom" analyzer. Lowercase is done by QueryParser for fuzzy search.
     *
     * @param word word
     * @return word escaped
     */
    private String escapeForFuzzy(String word) {
        int length = word.length();
        char[] tmp = new char[length * 4];
        length = ASCIIFoldingFilter.foldToASCII(word.toCharArray(), 0, tmp, 0, length);
        return new String(tmp, 0, length);
    }
}
