package org.database.query;

import org.database.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryManager implements IQueryManager {
    private static final Pattern CREATE_TABLE_PATTERN = Pattern.compile(
            "[cC][rR][eE][aA][tT][eE]\\s[tT][aA][bB][lL][eE]\\s(([a-zA-Z_])+)\\s\\((([a-zA-Z_,\\s\\d])+)\\)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern INSERT_TABLE_PATTERN = Pattern.compile(
            "[iI][nN][sS][eE][rR][tT]\\s[iI][nN][tT][oO]\\s([a-zA-Z\\d_]+)\\s[vV][aA][lL][uU][eE][sS]\\s\\(([a-zA-Z\\d_,]+)\\)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SELECT_TABLE_PATTERN = Pattern.compile(
            "[sS][eE][lL][eE][cC][tT]\\s(\\*|([a-zA-Z_,])+)\\s[fF][rR][oO][mM]\\s(([a-zA-Z_])+)(\\s([wW][hH][eE][rR][eE])\\s([a-zA-Z_=\\d\\s]+))*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern UPDATE_TABLE_PATTERN = Pattern.compile(
            "[uU][pP][dD][aA][tT][eE]\\s([a-zA-Z_]+)\\s[sS][eE][tT]\\s([a-zA-Z_=\\d,]+)(\\s([wW][hH][eE][rR][eE])\\s([a-zA-Z_=\\d\\s]+))*",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DELETE_TABLE_PATTERN = Pattern.compile(
            "[dD][eE][lL][eE][tT][eE]\\s[fF][rR][oO][mM]\\s(([a-zA-Z_])+)(\\s([wW][hH][eE][rR][eE])\\s([a-zA-Z_=\\d\\s]+))*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BEGIN_TRANSACTION_PATTERN = Pattern.compile(
            "[bB][eE][gG][iI][nN]\\s[tT][rR][aA][nN][sS][aA][cC][tT][iI][oO][nN][;]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern END_TRANSACTION_PATTERN = Pattern.compile(
            "[eE][nN][dD]\s[tT][rR][aA][nN][sS][aA][cC][tT][iI][oO][nN][;]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern COMMIT_TRANSACTION_PATTERN = Pattern.compile(
            "[cC][oO][mM][mM][iI][tT][;]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ROLLBACK_TRANSACTION_PATTERN = Pattern.compile(
            "[rR][oO][lL][lL][bB][aA][cC][kK][;]",
            Pattern.CASE_INSENSITIVE
    );

    QueryHandler queryHandler = new QueryHandler();

    /**
     Executes the provided query using the QueryUtils object and returns the result.
     @param query the query to be executed
     @param queryUtils the QueryUtils object that provides utility methods for query execution
     @return an integer representing the result of the query execution
     */
    public int runner(String query, QueryUtils queryUtils) {
        if(!Utils.isValidString(query)) return 0;

        Matcher matcher;

        matcher = BEGIN_TRANSACTION_PATTERN.matcher(query);
        if(matcher.find()) {
            queryUtils.setTransactionBegin(true);
            queryUtils.setTransactionClose(false);
            return 1;
        }

        matcher = END_TRANSACTION_PATTERN.matcher(query);
        if(matcher.find()) {
            queryUtils.setTransactionBegin(false);
            queryUtils.setTransactionClose(true);
            return 1;
        }

        matcher = COMMIT_TRANSACTION_PATTERN.matcher(query);
        if(matcher.find()) {
            if(!queryUtils.isTransactionBegin() && queryUtils.isTransactionClose()) {
                for(String pendingQuery: queryUtils.getTransactionQueryList()) {
                    runner(pendingQuery, new QueryUtils());
                }
                queryUtils.setTransactionClose(false);
                queryUtils.setTransactionQueryList(new ArrayList<>());
            }
            return 1;
        }

        matcher = ROLLBACK_TRANSACTION_PATTERN.matcher(query);
        if(matcher.find()) {
            queryUtils.setTransactionClose(false);
            queryUtils.setTransactionBegin(false);
            queryUtils.setTransactionQueryList(new ArrayList<>());
            return 1;
        }

        if(queryUtils.isTransactionBegin() && !queryUtils.isTransactionClose()) {
            List<String> tempList = queryUtils.getTransactionQueryList();
            tempList.add(query);
            queryUtils.setTransactionQueryList(tempList);
            return 1;
        } else {
            matcher = CREATE_TABLE_PATTERN.matcher(query);
            if(matcher.find()) {
                queryHandler.createQuery(matcher.group(1), matcher.group(3));
                return 1;
            }

            matcher = SELECT_TABLE_PATTERN.matcher(query);
            if(matcher.find()) {
                queryHandler.selectQuery(matcher.group(3), matcher.group(1), matcher.groupCount() < 5 ? null : matcher.group(7));
                return 1;
            }

            matcher = INSERT_TABLE_PATTERN.matcher(query);
            if(matcher.find()) {
                queryHandler.insertQuery(matcher.group(1), matcher.group(2));
                return 1;
            }

            matcher = UPDATE_TABLE_PATTERN.matcher(query);
            if(matcher.find()) {
                queryHandler.updateQuery(matcher.group(1), matcher.group(2), matcher.group(5));
                return 1;
            }

            matcher = DELETE_TABLE_PATTERN.matcher(query);
            if(matcher.find()) {
                queryHandler.deleteQuery(matcher.group(1), matcher.group(5));
                return 1;
            }
        }
        return 0;
    }
}
