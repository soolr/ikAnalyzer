package org.wltea.analyzer.lucene;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.Tokenizer;

public final class IKAnalyzer
  extends Analyzer
{
  private boolean useSmart;
  
  public boolean useSmart()
  {
    return this.useSmart;
  }
  
  public void setUseSmart(boolean useSmart)
  {
    this.useSmart = useSmart;
  }
  
  public IKAnalyzer()
  {
    this(false);
  }
  
  public IKAnalyzer(boolean useSmart)
  {
    this.useSmart = useSmart;
  }
  
  protected Analyzer.TokenStreamComponents createComponents(String text)
  {
    Reader reader = new BufferedReader(new StringReader(text));
    Tokenizer _IKTokenizer = new IKTokenizer(reader, useSmart());
    return new Analyzer.TokenStreamComponents(_IKTokenizer);
  }
}
