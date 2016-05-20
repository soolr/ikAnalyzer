package org.wltea.analyzer.query;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

public class IKQueryExpressionParser
{
  private List<Element> elements = new ArrayList();
  private Stack<Query> querys = new Stack();
  private Stack<Element> operates = new Stack();
  
  public Query parseExp(String expression, boolean quickMode)
  {
    Query lucenceQuery = null;
    if ((expression != null) && (!"".equals(expression.trim())))
    {
      try
      {
        splitElements(expression);
        
        parseSyntax(quickMode);
        if (this.querys.size() == 1) {
          lucenceQuery = (Query)this.querys.pop();
        } else {
          throw new IllegalStateException("表达式异常： 缺少逻辑操作符 或 括号缺失");
        }
      }
      finally
      {
        this.elements.clear();
        this.querys.clear();
        this.operates.clear();
      }
      this.elements.clear();
      this.querys.clear();
      this.operates.clear();
    }
    return lucenceQuery;
  }
  
  private void splitElements(String expression)
  {
    if (expression == null) {
      return;
    }
    Element curretElement = null;
    
    char[] expChars = expression.toCharArray();
    for (int i = 0; i < expChars.length; i++) {
      switch (expChars[i])
      {
      case '&': 
        if (curretElement == null)
        {
          curretElement = new Element();
          curretElement.type = '&';
          curretElement.append(expChars[i]);
        }
        else if (curretElement.type == '&')
        {
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        else if (curretElement.type == '\'')
        {
          curretElement.append(expChars[i]);
        }
        else
        {
          this.elements.add(curretElement);
          curretElement = new Element();
          curretElement.type = '&';
          curretElement.append(expChars[i]);
        }
        break;
      case '|': 
        if (curretElement == null)
        {
          curretElement = new Element();
          curretElement.type = '|';
          curretElement.append(expChars[i]);
        }
        else if (curretElement.type == '|')
        {
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        else if (curretElement.type == '\'')
        {
          curretElement.append(expChars[i]);
        }
        else
        {
          this.elements.add(curretElement);
          curretElement = new Element();
          curretElement.type = '|';
          curretElement.append(expChars[i]);
        }
        break;
      case '-': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '-';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case '(': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '(';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case ')': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = ')';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case ':': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = ':';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case '=': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '=';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case ' ': 
        if (curretElement != null) {
          if (curretElement.type == '\'')
          {
            curretElement.append(expChars[i]);
          }
          else
          {
            this.elements.add(curretElement);
            curretElement = null;
          }
        }
        break;
      case '\'': 
        if (curretElement == null)
        {
          curretElement = new Element();
          curretElement.type = '\'';
        }
        else if (curretElement.type == '\'')
        {
          this.elements.add(curretElement);
          curretElement = null;
        }
        else
        {
          this.elements.add(curretElement);
          curretElement = new Element();
          curretElement.type = '\'';
        }
        break;
      case '[': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '[';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case ']': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = ']';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case '{': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '{';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case '}': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = '}';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      case ',': 
        if (curretElement != null)
        {
          if (curretElement.type == '\'') {
            curretElement.append(expChars[i]);
          } else {
            this.elements.add(curretElement);
          }
        }
        else
        {
          curretElement = new Element();
          curretElement.type = ',';
          curretElement.append(expChars[i]);
          this.elements.add(curretElement);
          curretElement = null;
        }
        break;
      default: 
        if (curretElement == null)
        {
          curretElement = new Element();
          curretElement.type = 'F';
          curretElement.append(expChars[i]);
        }
        else if (curretElement.type == 'F')
        {
          curretElement.append(expChars[i]);
        }
        else if (curretElement.type == '\'')
        {
          curretElement.append(expChars[i]);
        }
        else
        {
          this.elements.add(curretElement);
          curretElement = new Element();
          curretElement.type = 'F';
          curretElement.append(expChars[i]);
        }
        break;
      }
    }
    if (curretElement != null)
    {
      this.elements.add(curretElement);
      curretElement = null;
    }
  }
  
  private void parseSyntax(boolean quickMode)
  {
    for (int i = 0; i < this.elements.size(); i++)
    {
      Element e = (Element)this.elements.get(i);
      if ('F' == e.type)
      {
        Element e2 = (Element)this.elements.get(i + 1);
        if (('=' != e2.type) && (':' != e2.type)) {
          throw new IllegalStateException("表达式异常： = 或 ： 号丢失");
        }
        Element e3 = (Element)this.elements.get(i + 2);
        if ('\'' == e3.type)
        {
          i += 2;
          if ('=' == e2.type)
          {
            TermQuery tQuery = new TermQuery(new Term(e.toString(), e3.toString()));
            this.querys.push(tQuery);
          }
          else if (':' == e2.type)
          {
            String keyword = e3.toString();
            
            Query _SWMCQuery = SWMCQueryBuilder.create(e.toString(), keyword, quickMode);
            this.querys.push(_SWMCQuery);
          }
        }
        else if (('[' == e3.type) || ('{' == e3.type))
        {
          i += 2;
          
          LinkedList<Element> eQueue = new LinkedList();
          eQueue.add(e3);
          for (i++; i < this.elements.size(); i++)
          {
            Element eN = (Element)this.elements.get(i);
            eQueue.add(eN);
            if ((']' == eN.type) || ('}' == eN.type)) {
              break;
            }
          }
          Query rangeQuery = toTermRangeQuery(e, eQueue);
          this.querys.push(rangeQuery);
        }
        else
        {
          throw new IllegalStateException("表达式异常：匹配值丢失");
        }
      }
      else if ('(' == e.type)
      {
        this.operates.push(e);
      }
      else if (')' == e.type)
      {
        boolean doPop = true;
        do
        {
          Element op = (Element)this.operates.pop();
          if ('(' == op.type)
          {
            doPop = false;
          }
          else
          {
            Query q = toBooleanQuery(op);
            this.querys.push(q);
          }
          if (!doPop) {
            break;
          }
        } while (!this.operates.empty());
      }
      else if (this.operates.isEmpty())
      {
        this.operates.push(e);
      }
      else
      {
        boolean doPeek = true;
        while ((doPeek) && (!this.operates.isEmpty()))
        {
          Element eleOnTop = (Element)this.operates.peek();
          if ('(' == eleOnTop.type)
          {
            doPeek = false;
            this.operates.push(e);
          }
          else if (compare(e, eleOnTop) == 1)
          {
            this.operates.push(e);
            doPeek = false;
          }
          else if (compare(e, eleOnTop) == 0)
          {
            Query q = toBooleanQuery(eleOnTop);
            this.operates.pop();
            this.querys.push(q);
          }
          else
          {
            Query q = toBooleanQuery(eleOnTop);
            this.operates.pop();
            this.querys.push(q);
          }
        }
        if ((doPeek) && (this.operates.empty())) {
          this.operates.push(e);
        }
      }
    }
    while (!this.operates.isEmpty())
    {
      Element eleOnTop = (Element)this.operates.pop();
      Query q = toBooleanQuery(eleOnTop);
      this.querys.push(q);
    }
  }
  
  private Query toBooleanQuery(Element op)
  {
    if (this.querys.size() == 0) {
      return null;
    }
    BooleanQuery resultQuery = new BooleanQuery();
    if (this.querys.size() == 1) {
      return (Query)this.querys.get(0);
    }
    Query q2 = (Query)this.querys.pop();
    Query q1 = (Query)this.querys.pop();
    if ('&' == op.type)
    {
      if (q1 != null) {
        if ((q1 instanceof BooleanQuery))
        {
          BooleanClause[] clauses = ((BooleanQuery)q1).getClauses();
          if ((clauses.length > 0) && 
            (clauses[0].getOccur() == BooleanClause.Occur.MUST)) {
            for (BooleanClause c : clauses) {
              resultQuery.add(c);
            }
          } else {
            resultQuery.add(q1, BooleanClause.Occur.MUST);
          }
        }
        else
        {
          resultQuery.add(q1, BooleanClause.Occur.MUST);
        }
      }
      if (q2 != null) {
        if ((q2 instanceof BooleanQuery))
        {
          BooleanClause[] clauses = ((BooleanQuery)q2).getClauses();
          if ((clauses.length > 0) && 
            (clauses[0].getOccur() == BooleanClause.Occur.MUST)) {
            for (BooleanClause c : clauses) {
              resultQuery.add(c);
            }
          } else {
            resultQuery.add(q2, BooleanClause.Occur.MUST);
          }
        }
        else
        {
          resultQuery.add(q2, BooleanClause.Occur.MUST);
        }
      }
    }
    else if ('|' == op.type)
    {
      if (q1 != null) {
        if ((q1 instanceof BooleanQuery))
        {
          BooleanClause[] clauses = ((BooleanQuery)q1).getClauses();
          if ((clauses.length > 0) && 
            (clauses[0].getOccur() == BooleanClause.Occur.SHOULD)) {
            for (BooleanClause c : clauses) {
              resultQuery.add(c);
            }
          } else {
            resultQuery.add(q1, BooleanClause.Occur.SHOULD);
          }
        }
        else
        {
          resultQuery.add(q1, BooleanClause.Occur.SHOULD);
        }
      }
      if (q2 != null) {
        if ((q2 instanceof BooleanQuery))
        {
          BooleanClause[] clauses = ((BooleanQuery)q2).getClauses();
          if ((clauses.length > 0) && 
            (clauses[0].getOccur() == BooleanClause.Occur.SHOULD)) {
            for (BooleanClause c : clauses) {
              resultQuery.add(c);
            }
          } else {
            resultQuery.add(q2, BooleanClause.Occur.SHOULD);
          }
        }
        else
        {
          resultQuery.add(q2, BooleanClause.Occur.SHOULD);
        }
      }
    }
    else if ('-' == op.type)
    {
      if ((q1 == null) || (q2 == null)) {
        throw new IllegalStateException("表达式异常：SubQuery 个数不匹配");
      }
      if ((q1 instanceof BooleanQuery))
      {
        BooleanClause[] clauses = ((BooleanQuery)q1).getClauses();
        if (clauses.length > 0) {
          for (BooleanClause c : clauses) {
            resultQuery.add(c);
          }
        } else {
          resultQuery.add(q1, BooleanClause.Occur.MUST);
        }
      }
      else
      {
        resultQuery.add(q1, BooleanClause.Occur.MUST);
      }
      resultQuery.add(q2, BooleanClause.Occur.MUST_NOT);
    }
    return resultQuery;
  }
  
  private TermRangeQuery toTermRangeQuery(Element fieldNameEle, LinkedList<Element> elements)
  {
    boolean includeFirst = false;
    boolean includeLast = false;
    String firstValue = null;
    String lastValue = null;
    
    Element first = (Element)elements.getFirst();
    if ('[' == first.type) {
      includeFirst = true;
    } else if ('{' == first.type) {
      includeFirst = false;
    } else {
      throw new IllegalStateException("表达式异常");
    }
    Element last = (Element)elements.getLast();
    if (']' == last.type) {
      includeLast = true;
    } else if ('}' == last.type) {
      includeLast = false;
    } else {
      throw new IllegalStateException("表达式异常, RangeQuery缺少结束括号");
    }
    if ((elements.size() < 4) || (elements.size() > 5)) {
      throw new IllegalStateException("表达式异常, RangeQuery 错误");
    }
    Element e2 = (Element)elements.get(1);
    if ('\'' == e2.type)
    {
      firstValue = e2.toString();
      
      Element e3 = (Element)elements.get(2);
      if (',' != e3.type) {
        throw new IllegalStateException("表达式异常, RangeQuery缺少逗号分隔");
      }
      Element e4 = (Element)elements.get(3);
      if ('\'' == e4.type) {
        lastValue = e4.toString();
      } else if (e4 != last) {
        throw new IllegalStateException("表达式异常，RangeQuery格式错误");
      }
    }
    else if (',' == e2.type)
    {
      firstValue = null;
      
      Element e3 = (Element)elements.get(2);
      if ('\'' == e3.type) {
        lastValue = e3.toString();
      } else {
        throw new IllegalStateException("表达式异常，RangeQuery格式错误");
      }
    }
    else
    {
      throw new IllegalStateException("表达式异常, RangeQuery格式错误");
    }
    return new TermRangeQuery(fieldNameEle.toString(), new BytesRef(firstValue), new BytesRef(lastValue), includeFirst, includeLast);
  }
  
  private int compare(Element e1, Element e2)
  {
    if ('&' == e1.type)
    {
      if ('&' == e2.type) {
        return 0;
      }
      return 1;
    }
    if ('|' == e1.type)
    {
      if ('&' == e2.type) {
        return -1;
      }
      if ('|' == e2.type) {
        return 0;
      }
      return 1;
    }
    if ('-' == e2.type) {
      return 0;
    }
    return -1;
  }
  
  private class Element
  {
    char type = '\000';
    StringBuffer eleTextBuff;
    
    public Element()
    {
      this.eleTextBuff = new StringBuffer();
    }
    
    public void append(char c)
    {
      this.eleTextBuff.append(c);
    }
    
    public String toString()
    {
      return this.eleTextBuff.toString();
    }
  }
  
  public static void main(String[] args)
  {
    IKQueryExpressionParser parser = new IKQueryExpressionParser();
    
    String ikQueryExp = "(id='ABcdRf' && date:{'20010101','20110101'} && keyword:'魔兽中国') || (content:'KSHT-KSH-A001-18'  || ulr='www.ik.com') - name:'林良益'";
    Query result = parser.parseExp(ikQueryExp, true);
    System.out.println(result);
  }
}
