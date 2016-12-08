package com.sillyhat.mybatis.interceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.sillyhat.mybatis.dto.PageDTO;
import com.sillyhat.mybatis.utils.PageConstants;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathNotFoundException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
        @Signature(type=Executor.class,method="query",args={
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        })
})
public class PageInterceptor implements Interceptor {
    private String changeSql(PageDTO page, String sql){
        Map<String, String> params = page.getParams();
        for(Iterator iter = params.keySet().iterator(); iter.hasNext();){
            Object key = iter.next();
            Object val = params.get(key);
//            if(){
//
//            }
            System.out.println( "key:" + key);
            System.out.println( "value:" + val);
        }
        return sql;
    }
    public Object intercept(Invocation invocation) throws Throwable {
        // 当前环境 MappedStatement，BoundSql，及sql取得
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql().trim();
        Object parameterObject = boundSql.getParameterObject();
        // Page对象获取，“信使”到达拦截器！
        PageDTO page = searchPageWithXpath(boundSql.getParameterObject(), ".","page", "*/page");
        if (page != null) {
            // Page对象存在的场合，开始分页处理
            String countSql = getCountSql(originalSql);
            originalSql = changeSql(page, originalSql);
            String orderDirection = page.getOrderDirection();
            String orderField = page.getOrderField();
            if(orderField != null && !"".equals(orderField)){
                if(originalSql.indexOf("order") != -1){
                    originalSql = originalSql.substring(0, originalSql.indexOf("order")) + " order by " + orderField + " " + orderDirection;
                }else{
                    originalSql += " order by " + orderField + " " + orderDirection;
                }
            }
            Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
            PreparedStatement countStmt = connection.prepareStatement(countSql);
            BoundSql countBS = copyFromBoundSql(mappedStatement, boundSql,countSql);
            DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject, countBS);
            parameterHandler.setParameters(countStmt);
            ResultSet rs = countStmt.executeQuery();
            int totpage = 0;
            if (rs.next()) {
                totpage = rs.getInt(1);
            }
            rs.close();
            countStmt.close();
            connection.close();
            // 分页计算
            int pageNo = page.getPageNo();//当前页码
            int pageSize = page.getPageSize();//每页行数
            page.setTotalRecord(totpage);
            page.setTotalPage(getLastPage(totpage, pageSize));
            StringBuffer sql = null;
//            if(ReadDatabaseProperties.getValueByKey(PageConstants.DATABASE_0).equals(PageConstants.DATABASE_SORT_ORACLE)){
//                sql = getOracleSql(originalSql, pageNo,pageSize,totpage);
//            }else if(ReadDatabaseProperties.getValueByKey(PageConstants.DATABASE_0).equals(PageConstants.DATABASE_SORT_MYSQL)){
//                sql = getMysqlSql(originalSql, pageNo,pageSize,totpage);
//            }
            sql = getOracleSql(originalSql, pageNo,pageSize,totpage);
            BoundSql newBoundSql = copyFromBoundSql(mappedStatement, boundSql,sql.toString());
            MappedStatement newMs = copyFromMappedStatement(mappedStatement,new BoundSqlSqlSource(newBoundSql));
            invocation.getArgs()[0] = newMs;
        }
        return invocation.proceed();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {

    }

    /**
     * <p>Title: getLastPage</p>
     * <p>Description: </p>得到最后一页的页数
     * @param @param paging
     * @param @return
     * @author 徐士宽
     * @date 2015-1-23
     * @return:int
     */
    private int getLastPage(int totpage,int pageSize){
        int lastPage = totpage%pageSize == 0 ? (totpage/pageSize) : (totpage/pageSize + 1);
        //没有查询到结果时
        if(lastPage == 0){
            lastPage = 1;
        }
        return lastPage;
    }
    /**
     * <p>Title: getOracleSql</p>
     * <p>Description: </p>TODO
     * @param @param originalSql	原始SQL
     * @param @param pageNo	当前页码
     * @param @param pageSize	每页行数
     * @param @param totalRecord	总记录数
     * @author 徐士宽
     * @date 2015-4-10
     * @return:StringBuffer
     */
    private StringBuffer getOracleSql(String originalSql,int pageNo,int pageSize,int totalRecord){
        int startRecord = pageNo * pageSize - pageSize + 1;
        int endRecord = pageNo * pageSize;
        StringBuffer sql = new StringBuffer();
        sql.append("select * from (select t.*, rownum rn from (").append(originalSql);
        //判断排序
        sql.append("");
        sql.append(") t where rownum <= ").append(endRecord).append(") where rn >= ").append(startRecord);
        return sql;
    }
    /**
     * <p>Title: getMysqlSql</p>
     * <p>Description: </p>TODO
     * @param @param originalSql	原始SQL
     * @param @param pageNo	当前页码
     * @param @param pageSize	每页行数
     * @author 徐士宽
     * @date 2015-4-10
     * @return:StringBuffer
     */
    private StringBuffer getMysqlSql(String originalSql,int pageNo,int pageSize,int totalRecord){
        int offset = (pageNo - 1) * pageSize;
        StringBuffer sql = new StringBuffer();
        sql.append(originalSql).append(" limit ").append(offset).append(",").append(pageSize);
        return sql;
    }
    /**
     * 根据给定的xpath查询Page对象
     */
    private PageDTO searchPageWithXpath(Object o, String... xpaths) {
        JXPathContext context = JXPathContext.newContext(o);
        Object result;
        for (String xpath : xpaths) {
            try {
                result = context.selectSingleNode(xpath);
            } catch (JXPathNotFoundException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
            if (result instanceof PageDTO) {
                return (PageDTO) result;
            }
        }
        return null;
    }

    /**
     * 复制MappedStatement对象
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms,
                                                    SqlSource newSqlSource) {
        Builder builder = new Builder(ms.getConfiguration(), ms.getId(),newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
//		builder.keyProperty(ms.getKeyProperty());
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * 复制BoundSql对象
     */
    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
                                      String sql) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql,
                boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop,
                        boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    /**
     * 根据原Sql语句获取对应的查询总记录数的Sql语句
     */
    private String getCountSql(String sql) {
        return "SELECT COUNT(*) FROM (" + sql + ") aliasForPage";
    }

    public class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
