package org.wltea.analyzer.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class SWMCQueryBuilder
{
  public static Query create(String fieldName, String keywords, boolean quickMode)
  {
    if ((fieldName == null) || (keywords == null)) {
      throw new IllegalArgumentException("参数 fieldName 、 keywords 不能为null.");
    }
    List<Lexeme> lexemes = doAnalyze(keywords);
    
    Query _SWMCQuery = getSWMCQuery(fieldName, lexemes, quickMode);
    return _SWMCQuery;
  }
  
  private static List<Lexeme> doAnalyze(String keywords)
  {
    List<Lexeme> lexemes = new ArrayList();
    IKSegmenter ikSeg = new IKSegmenter(new StringReader(keywords), true);
    try
    {
      Lexeme l = null;
      while ((l = ikSeg.next()) != null) {
        lexemes.add(l);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return lexemes;
  }
  
  private static Query getSWMCQuery(String fieldName, List<Lexeme> lexemes, boolean quickMode)
  {
    StringBuffer keywordBuffer = new StringBuffer();
    
    StringBuffer keywordBuffer_Short = new StringBuffer();
    
    int lastLexemeLength = 0;
    
    int lastLexemeEnd = -1;
    
    int shortCount = 0;
    int totalCount = 0;
    for (Lexeme l : lexemes)
    {
      totalCount += l.getLength();
      if (l.getLength() > 1)
      {
        keywordBuffer_Short.append(' ').append(l.getLexemeText());
        shortCount += l.getLength();
      }
      if (lastLexemeLength == 0) {
        keywordBuffer.append(l.getLexemeText());
      } else if ((lastLexemeLength == 1) && (l.getLength() == 1) && 
        (lastLexemeEnd == l.getBeginPosition())) {
        keywordBuffer.append(l.getLexemeText());
      } else {
        keywordBuffer.append(' ').append(l.getLexemeText());
      }
      lastLexemeLength = l.getLength();
      lastLexemeEnd = l.getEndPosition();
    }
    QueryParser qp = new QueryParser(fieldName, new StandardAnalyzer());
    qp.setDefaultOperator(QueryParser.AND_OPERATOR);
    qp.setAutoGeneratePhraseQueries(true);
    if ((quickMode) && (shortCount * 1.0F / totalCount > 0.5F)) {
      try
      {
        return qp.parse(keywordBuffer_Short.toString());
      }
      catch (ParseException e)
      {
        e.printStackTrace();
      }
    } else if (keywordBuffer.length() > 0) {
      try
      {
        return qp.parse(keywordBuffer.toString());
      }
      catch (ParseException e)
      {
        e.printStackTrace();
      }
    }
    return null;
  }
}
