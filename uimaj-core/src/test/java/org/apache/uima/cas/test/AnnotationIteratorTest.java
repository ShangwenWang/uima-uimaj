/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.uima.cas.test;

import static org.apache.uima.cas.SelectFSs.select;
import static org.apache.uima.cas.impl.Subiterator.BoundsUse.coveredBy;
import static org.apache.uima.cas.impl.Subiterator.BoundsUse.covering;
import static org.apache.uima.cas.impl.Subiterator.BoundsUse.notBounded;
import static org.apache.uima.cas.impl.Subiterator.BoundsUse.sameBeginEnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.SelectFSs;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.Subiterator.BoundsUse;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Setup:  all kinds of types, primitives and non-primitives
 *         see CASTestSetup class
 *         
 *         Multiple Indexes, some sorted\
 *         
 *         setupTheCas - puts in tokens / phrases / sentences, overlapping
 *           tokens 0-4, 1-5, 2-6, etc.
 *           sentences 0-10, 5-15, 10-20, etc.    
 *             + 12-31
 *           phrases  0-5,  6-9,  10-16, 14-19, ...
 */
public class AnnotationIteratorTest {
  
  private static final boolean showFSs = true;

  private CAS cas;
  
  private TypeSystem ts;
  private Type stringType;
  private Type tokenType;
  private Type intType;
  private Type tokenTypeType;
  private Type wordType;
  private Type sepType;
  private Type eosType;
  private Type sentenceType;
  private Type phraseType;

  private Feature tokenTypeFeat;
  private Feature lemmaFeat;
  private Feature sentLenFeat;
  private Feature tokenFloatFeat;
  private Feature startFeature;
  private Feature endFeature;

  private boolean isSave;
  private List<Annotation> fss;
  private List<Integer> fssStarts = new ArrayList<>();
  private int callCount = -1;
  private Type[] types = new Type[3];

  @Before
  public void setUp() throws Exception {
    // make a cas with various types, fairly complex -- see CASTestSetup class
    cas = CASInitializer.initCas(new CASTestSetup(), null);
    assertTrue(cas != null);
    this.ts = cas.getTypeSystem();
    assertTrue(this.ts != null);

    this.stringType = this.ts.getType(CAS.TYPE_NAME_STRING);
    assertTrue(this.stringType != null);
    this.tokenType = this.ts.getType(CASTestSetup.TOKEN_TYPE);
    assertTrue(this.stringType != null);
    this.intType = this.ts.getType(CAS.TYPE_NAME_INTEGER);
    assertTrue(this.intType != null);
    this.tokenTypeType = this.ts.getType(CASTestSetup.TOKEN_TYPE_TYPE);
    assertTrue(this.tokenTypeType != null);
    this.wordType = this.ts.getType(CASTestSetup.WORD_TYPE);
    assertTrue(this.wordType != null);
    this.sepType = this.ts.getType(CASTestSetup.SEP_TYPE);
    assertTrue(this.sepType != null);
    this.eosType = this.ts.getType(CASTestSetup.EOS_TYPE);
    assertTrue(this.eosType != null);
    this.tokenTypeFeat = this.ts.getFeatureByFullName(CASTestSetup.TOKEN_TYPE_FEAT_Q);
    assertTrue(this.tokenTypeFeat != null);
    this.lemmaFeat = this.ts.getFeatureByFullName(CASTestSetup.LEMMA_FEAT_Q);
    assertTrue(this.lemmaFeat != null);
    this.sentLenFeat = this.ts.getFeatureByFullName(CASTestSetup.SENT_LEN_FEAT_Q);
    assertTrue(this.sentLenFeat != null);
    this.tokenFloatFeat = this.ts.getFeatureByFullName(CASTestSetup.TOKEN_FLOAT_FEAT_Q);
    assertTrue(this.tokenFloatFeat != null);
    this.startFeature = this.ts.getFeatureByFullName(CAS.FEATURE_FULL_NAME_BEGIN);
    assertTrue(this.startFeature != null);
    this.endFeature = this.ts.getFeatureByFullName(CAS.FEATURE_FULL_NAME_END);
    assertTrue(this.endFeature != null);
    this.sentenceType = this.ts.getType(CASTestSetup.SENT_TYPE);
    assertTrue(this.sentenceType != null);
    this.phraseType = this.ts.getType(CASTestSetup.PHRASE_TYPE);
    assertTrue(this.phraseType != null);
    types[0] = sentenceType;
    types[1] = phraseType;
    types[2] = tokenType;
  }

  @After
  public void tearDown() {
    cas = null;
    this.ts = null;
    this.tokenType = null;
    this.intType = null;
    this.tokenTypeType = null;
    this.wordType = null;
    this.sepType = null;
    this.eosType = null;
    this.tokenTypeFeat = null;
    this.lemmaFeat = null;
    this.sentLenFeat = null;
    this.tokenFloatFeat = null;
    this.startFeature = null;
    this.endFeature = null;
    this.sentenceType = null;
  }
  
//  //debug
//  // explore which isValid calls can be eliminated
//  public void testIsValid() {
//    int annotCount = setupTheCas();
//    FSIndexRepository ir = cas.getIndexRepository();
//    
//    FSIterator<AnnotationFS> it = cas.getAnnotationIndex().iterator();
//    it.moveToLast();
//    int c = 0;
//    while (it.hasPrevious()) {
//      it.previous();
//      c++;
//    }
//    System.out.println("debug count = " + c);
//  }
  

  @Test
  public void testIterator1() throws Exception {
    final int annotCount = setupTheCas();
    FSIndexRepository indexRepository = cas.getIndexRepository();

    /***************************************************
     * iterate over them
     ***************************************************/
    fss = new ArrayList<>();
    callCount = -1;
    iterateOverAnnotations(annotCount, fss); // annotCount is the total number of sentences and tokens
    
    callCount = -1;
    iterateOverAnnotations(annotCount, fss);  // should be using flattened version
    
    /***************************************************
     * test skipping over multiple equal items at front
     ***************************************************/
    callCount = -1;
    fss.clear();
    isSave = true;
    
    AnnotationFS a1 = cas.createAnnotation(this.tokenType, 1, 6);
    a1.setStringValue(lemmaFeat, "lemma1");
    indexRepository.addFS(a1);
    
    AnnotationFS a2 = cas.createAnnotation(this.tokenType, 1, 6);
    a2.setStringValue(lemmaFeat, "lemma2");
    indexRepository.addFS(a2);
    
    AnnotationIndex<Annotation> tokenIndex = cas.getAnnotationIndex(tokenType);
    FSIterator<Annotation> it = tokenIndex.subiterator(a1);
    assertCount("multi equal", 0, it);
    
    FSIterator<Annotation> it2 = tokenIndex.subiterator(a1);
    // make a new iterator that hasn't been converted to a list form internally
    it2.moveTo(cas.getDocumentAnnotation());
    assertFalse(it2.isValid()); 
  }
  
  /**
   * The tests include:
   *   a) running with / w/o "flattened" indexes
   *   b) running forwards and backwards (testing moveToLast, isValid)
   *   c) testing strict and unambiguous variants
   *   d) running over all annotations and restricting to just a particular subtype
   *   
   *   new tests:  
   *     verifying bounding FS < all returned, including multiples of it
   *     strict at 1st element, at last element
   *     (not done yet) ConcurrentModificationException testing
   *     
   *     (not done yet) Testing with different bound styles
   *     
   * @param annotCount -
   * @param afss -
   */
  // called twice, the 2nd time should be with flattened indexes (List afss non empty the 2nd time)
  private void iterateOverAnnotations(final int annotCount, List<Annotation> afss) throws Exception {
    this.fss = afss;
    isSave = fss.size() == 0;   // on first call is 0, so save on first call
    
//    int count;
    AnnotationIndex<Annotation> annotIndex = cas.getAnnotationIndex();
    AnnotationIndex<Annotation> sentIndex = cas.getAnnotationIndex(sentenceType);
    
//    assertTrue((isSave) ? it instanceof FSIteratorWrapper : 
//      FSIndexFlat.enabled ? it instanceof FSIndexFlat.FSIteratorFlat : it instanceof FSIteratorWrapper);   
    assertCount("Normal ambiguous annot iterator", annotCount, annotIndex.iterator(true));
     // a normal "ambiguous" iterator
    assertCount("Normal ambiguous select annot iterator", annotCount, annotIndex.select().fsIterator());
    assertEquals(annotCount, select(annotIndex).toArray().length);  // stream op
    assertEquals(annotCount, select(annotIndex).asArray(Annotation.class).length);  // select op
    assertEquals(annotCount - 5, annotIndex.select().startAt(2).asArray(Annotation.class).length);
    
    Annotation[] tokensAndSentencesAndPhrases = annotIndex.select().asArray(Annotation.class);
    
    JCas jcas = cas.getJCas();
    
    FSArray<Annotation> fsa = FSArray.create(jcas, tokensAndSentencesAndPhrases);
    NonEmptyFSList<Annotation> fslhead = (NonEmptyFSList<Annotation>) FSList.<Annotation, Annotation>create(jcas,  tokensAndSentencesAndPhrases);
    
    assertCount("fsa ambiguous select annot iterator", annotCount, 
        fsa.select().fsIterator());

    assertCount("fslhead ambiguous select annot iterator", annotCount, 
        fslhead.<Annotation>select().fsIterator());
    
    // backwards
    assertCount("Normal select backwards ambiguous annot iterator", annotCount,
        select(annotIndex).backwards().fsIterator());
    
    // because of document Annotation - spans the whole range
    assertCount("Unambiguous annot iterator", 1, 
        // false means create an unambiguous iterator
        annotIndex.iterator(false));
    
    // because of document Annotation - spans the whole range
    assertCount("Unambiguous select annot iterator", 1, 
        annotIndex.select().nonOverlapping().fsIterator());
    
    // because of document Annotation - spans the whole range
    assertCount("Unambiguous select backwards annot iterator", 1,
        annotIndex.select().nonOverlapping().backwards(true).fsIterator());
    
    // false means create an unambiguous iterator
    assertCount("Unambigous sentence iterator", 5, 
        sentIndex.iterator(false));
    
    assertCount("Unambigous select sentence iterator", 5,
        annotIndex.select(sentenceType).nonOverlapping(true).fsIterator());
    assertCount("Unambigous select sentence iterator", 5, 
        sentIndex.select().nonOverlapping().fsIterator());
    assertCount("Unambigous select sentence iterator", 5, 
        sentIndex.select().nonOverlapping().fsIterator());
    
    AnnotationFS bigBound = cas.createAnnotation(this.sentenceType, 10, 41);
    // ambiguous, and strict
    assertCount("Subiterator over annot with big bound, strict", 38, 
        annotIndex.subiterator(bigBound, true, true));
    assertCount("Subiterator select over annot with big bound, strict", 38,
        annotIndex.select().coveredBy((Annotation) bigBound)
            .includeAnnotationsWithEndBeyondBounds(false).fsIterator());
    assertThat(annotIndex.select().coveredBy(bigBound).limit(7)
            .includeAnnotationsWithEndBeyondBounds().asList())
        .as("Subiterator select limit 7 over annot with big bound, strict")
        .extracting(a -> a.getType(), a -> a.getBegin(), a -> a.getEnd())
        .containsExactly(
            tuple(sentenceType, 10, 20),
            tuple(tokenType, 10, 15),
            tuple(tokenType, 11, 16),
            tuple(sentenceType, 12, 31),
            tuple(tokenType, 12, 17),
            tuple(tokenType, 13, 18),
            tuple(tokenType, 14, 19));
    assertCountLimit("Subiterator select limit 7 over annot with big bound, strict", 7,
        annotIndex.select().coveredBy(bigBound).limit(7)
            .includeAnnotationsWithEndBeyondBounds().fsIterator());
    
    // uncomment these to check compile-time generic arguments OK
    // comment these out for running, because Token not a type
//    FSIndex<Token> token_index = annotIndex.subType(Token.class);
//    token_index.select().fsIterator();
//    select(token_index).fsIterator();
//    annotIndex.select(Token.class).fsIterator();
//    cas.select(Token.class).fsIterator();
//    token_index.select(Token.class).fsIterator();
    
    assertThat(annotIndex.select().coveredBy(bigBound).skip(3).toArray())
        .hasSize(35);
    
    Object[] o1 = annotIndex.select()
        .coveredBy(bigBound)
        .toArray();
    List<Annotation> l2 = annotIndex.select()
        .coveredBy(bigBound)
        .backwards()
        .asList();
    Deque<Annotation> l2r = new ArrayDeque<>();
    for (Annotation fs : l2) {
      l2r.push(fs);
    }
    
    assertThat(o1)
        .isEqualTo(l2r.toArray());
    
    // unambiguous, strict  bigBound= sentenceType 10-41
    assertCount("Subiterator over annot unambiguous strict", 3, 
        annotIndex.subiterator(bigBound, false, true));
    assertCount("Subiterator select over annot unambiguous strict", 3, 
        annotIndex.select().coveredBy((Annotation) bigBound)
            .includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator());
    assertCount("Subiterator select over annot unambiguous strict", 3, 
        annotIndex.select().backwards().coveredBy((Annotation) bigBound)
            .includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator());

//    it = annotIndex.subiterator(bigBound, true, false);
//    while (it.hasNext()) {
//      Annotation a = (Annotation) it.next();
//      System.out.format("debug %s:%d   b:%d e:%d%n", a.getType().getShortName(), a._id(), a.getBegin(), a.getEnd());
//    }

    assertCount("Subiterator over annot ambiguous not-strict", 46, 
        annotIndex.subiterator(bigBound, true, false));
    
    // covered by implies endWithinBounds
    assertCount("Subiterator select over annot ambiguous strict", 38, 
        select(annotIndex).coveredBy(bigBound).fsIterator());
    assertCount("Subiterator select over annot ambiguous strict", 38, 
        annotIndex.select().coveredBy(bigBound)
            .includeAnnotationsWithEndBeyondBounds(false).fsIterator());
    assertCount("Subiterator select over annot ambiguous not-strict", 46,
        select(annotIndex).coveredBy(bigBound)
            .includeAnnotationsWithEndBeyondBounds(true).fsIterator());
    
    // unambiguous, not strict
    assertCount("Subiterator over annot, unambiguous, not-strict", 4,
        annotIndex.subiterator(bigBound, false, false));
    assertCount("Subiterator select over annot unambiguous not-strict", 4, 
        select(annotIndex).nonOverlapping().coveredBy(bigBound)
            .includeAnnotationsWithEndBeyondBounds(true).fsIterator());
    
    AnnotationFS sent = cas.getAnnotationIndex(this.sentenceType).iterator().get();
    assertThat(annotIndex.subiterator(sent, false, true)).toIterable()
        .as("Subiterator over annot unambiguous strict")
        .extracting(a -> a.getType(), a -> a.getBegin(), a -> a.getEnd())
        .containsExactly(
            tuple(tokenType, 0, 5), 
            tuple(tokenType, 5, 10));
    assertCount("Subiterator over annot unambiguous strict", 2, 
        annotIndex.subiterator(sent, false, true));
    
    assertThat(annotIndex.select().nonOverlapping().coveredBy(sent).asList())
        .as("Subiterator select over annot unambiguous strict")
        .extracting(a -> a.getType(), a -> a.getBegin(), a -> a.getEnd())
        .containsExactly(
            tuple(tokenType, 0, 5), 
            tuple(tokenType, 5, 10));
    assertCount("Subiterator select over annot unambiguous strict", 2, 
        annotIndex.select().nonOverlapping().coveredBy(sent).fsIterator());
    
    // strict skips first item
    bigBound = cas.createAnnotation(this.sentenceType,  11, 30);
    assertCount("Subiteratover over sent ambiguous strict", 4, 
        sentIndex.subiterator(bigBound, true, true));
    assertCount("Subiteratover over sent ambiguous", 9, 
        sentIndex.subiterator(bigBound, true, false));
    assertCount("Subiteratover over sent unambiguous", 1, 
        sentIndex.subiterator(bigBound, false, false));
    
    // single, get, nullOK
    assertThat(annotIndex.select().nonOverlapping().get().getType().getShortName())
        .isEqualTo("DocumentAnnotation");
    assertThatExceptionOfType(CASRuntimeException.class)
        .isThrownBy(() -> annotIndex.select().nullOK(false).coveredBy(3, 3).get())
        .matches(e -> e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES));

    assertNull(annotIndex.select()
        .coveredBy(3, 3)
        .nullOK()
        .get());
    assertNotNull(annotIndex.select()
        .get(3));
    assertNull(annotIndex.select()
        .nullOK()
        .coveredBy(3, 5)
        .get(3));
    
    assertThatExceptionOfType(CASRuntimeException.class)
        .isThrownBy(() -> annotIndex.select().coveredBy(3, 5).get(3))
        .matches(e -> e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES));
    
    assertThat(annotIndex.select().nonOverlapping().get().getType().getShortName())
        .isEqualTo("DocumentAnnotation");
    
    // because of document Annotation - spans the whole range
    assertCount("Unambiguous select annot iterator", 1, 
        annotIndex.select().nonOverlapping().fsIterator());
    // because of document Annotation - spans the whole range
    assertCount("Unambiguous select backwards annot iterator", 1, 
        annotIndex.select().nonOverlapping().backwards(true).fsIterator());
    assertNotNull(annotIndex.select().nonOverlapping().single());

    assertThatExceptionOfType(CASRuntimeException.class)
        .isThrownBy(() -> annotIndex.select().coveredBy(3, 10).single())
        .matches(e -> e.hasMessageKey(CASRuntimeException.SELECT_GET_TOO_MANY_INSTANCES));

    assertThatExceptionOfType(CASRuntimeException.class)
        .isThrownBy(() -> annotIndex.select().coveredBy(3, 10).singleOrNull())
        .matches(e -> e.hasMessageKey(CASRuntimeException.SELECT_GET_TOO_MANY_INSTANCES));

    assertThatExceptionOfType(CASRuntimeException.class)
        .isThrownBy(() -> annotIndex.select().coveredBy(3, 5).single())
        .matches(e -> e.hasMessageKey(CASRuntimeException.SELECT_GET_NO_INSTANCES));
        
    annotIndex.select().coveredBy(3, 5).singleOrNull();
    
    assertCountLimit("Following", 2, 
        select(annotIndex).following(2, 39).limit(2).fsIterator());
    assertCountLimit("Following", 2,
        select(annotIndex).following(2, 39).backwards().limit(2).fsIterator());
    
    assertCount("select source array", 21, 
        fsa.select(sentenceType).fsIterator());
    assertCount("select source array", 21, 
        fslhead.select(sentenceType).fsIterator());
    
    /** covering **/
    annotIndex.select(sentenceType).covering(20, 30).forEachOrdered(f ->  
        System.out.format("found fs start at %d end %d%n", f.getBegin(), f.getEnd()));
    
    annotIndex.select(sentenceType).covering(15, 19).forEachOrdered(f ->  
        System.out.format("covering 15, 19:   %s:%d   %d -  %d%n", 
            f.getType().getShortName(), f._id(), f.getBegin(), f.getEnd()));

    annotIndex.select(sentenceType).covering(37, 39).forEachOrdered(f ->  
        System.out.format("covering sentences 37, 39:   %s:%d   %d -  %d%n", 
            f.getType().getShortName(), f._id(), f.getBegin(), f.getEnd()));

    annotIndex.select(phraseType).covering(15, 19).forEachOrdered(f ->  
       System.out.format("covering phrases 15, 19:   %s:%d   %d -  %d%n", 
           f.getType().getShortName(), f._id(), f.getBegin(), f.getEnd()));

    annotIndex.select(phraseType).covering(37, 39).forEachOrdered(f ->  
       System.out.format("covering phrases 37, 39:   %s:%d   %d -  %d%n", 
           f.getType().getShortName(), f._id(), f.getBegin(), f.getEnd()));
  }
  
  private String flatStateMsg(String s) {
    return s + (isSave ? "" : " with flattened index");
  }

  private void assertCount(String msg, int expected,  FSIterator<? extends Annotation> it) {
    int fssStart = assertCountCmn(msg, expected, it);
    msg = flatStateMsg(msg);
    int count = expected;
    if (count > 0) {
      // test moveTo(fs) in middle, first, and last
      
      AnnotationFS posFs = fss.get(fssStart + (count >> 1));
//      //debug
//      System.out.println(posFs.toString());
      // debug
      it.moveToLast();
      it.next();

      // Move to middle
      it.moveTo(posFs);
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      
      // Move to first
      posFs = fss.get(fssStart);
      it.moveTo(posFs);
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      it.moveToFirst();
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      
      // Move to last
      posFs = fss.get(fssStart + count - 1);
      it.moveTo(posFs);
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
      it.moveToLast();
      assertEquals(msg, it.get().hashCode(), posFs.hashCode());
    } else {
      // count is 0
      it.moveToFirst();
      assertFalse(it.isValid());
      it.moveToLast();
      assertFalse(it.isValid());
      it.moveTo(cas.getDocumentAnnotation());
      assertFalse(it.isValid());
    }
    
    // test movetoLast, moving backwards
    count = 0;
    for (it.moveToLast(); it.isValid(); it.moveToPrevious()) {
      ++count;
    }
    
    assertEquals(msg, expected, count);
  }
  
  // called by assertCount
  // called by asserCountLimit
  private int assertCountCmn(String msg, int expected, FSIterator<? extends Annotation> it) {
    // add with-flattened-index if isSave is false
    msg = flatStateMsg(msg);
    int count = 0;
    callCount  ++;
    
    int fssStart;
    if (isSave) {
      fssStarts.add(fssStart = fss.size());
    } else {
      fssStart = fssStarts.get(callCount);
    }
    
    while (it.isValid()) {
      ++count;
      Annotation fs = it.next();
      if (showFSs) {
        System.out.format("assertCountCmn: %2d %s   %10s  %d - %d%n", count, msg, 
            fs.getType().getName(), fs.getBegin(), fs.getEnd());
      }
      if (isSave) {
        fss.add(fs);
      } else {
        assertEquals(msg, fss.get(fssStart + count -1).hashCode(), fs.hashCode());
      }
    }
    
    assertEquals(msg, expected, count);
    return fssStart;
  }
  
  private void assertCountLimit(String msg, int expected,  FSIterator<? extends Annotation> it) {
    assertCountCmn(msg, expected, it);
    it.moveToFirst();
    assertFalse(it.isValid());
  }
  
  @Test
  public void testIncorrectIndexTypeException() {
    boolean caughtException = false;
    try {
      cas.getAnnotationIndex(this.stringType);
    } catch (CASRuntimeException e) {
//      e.printStackTrace();
      caughtException = true;
    }
    assertTrue(caughtException);
    
    caughtException = false;
    try {
    	cas.getAnnotationIndex(ts.getType(CASTestSetup.TOKEN_TYPE_TYPE));
    } catch (CASRuntimeException e) {
    	caughtException = true;
    }
    assertTrue(caughtException);
    try {
      cas.getAnnotationIndex(this.tokenType);
    } catch (CASRuntimeException e) {
      assertTrue(false);
    }
  }
  
  /**
   * UIMA-2808 - There was a bug in Subiterator causing the first annotation of the type of the
   * index the subiterator was applied to always to be returned, even if outside the boundary
   * annotation.
   */
  @Test
  public void testUnambiguousSubiteratorOnIndex() {
    try {
      //                        0    0    1    1    2    2    3    3    4    4    5
      //                        0    5    0    5    0    5    0    5    0    5    0
      //                        ------- sentence ---------
      //                                                  -------- sentence ---------
      //                                                                        -tk-
      cas.setDocumentText("Sentence A with no value. Sentence B with value 377.");
    } catch (CASRuntimeException e) {
      assertTrue(false);
    }   
    AnnotationIndex<Annotation> ai = cas.getAnnotationIndex();
          
    cas.addFsToIndexes(cas.createAnnotation(this.sentenceType, 0, 25));
    cas.addFsToIndexes(cas.createAnnotation(this.sentenceType, 26, 52));
    cas.addFsToIndexes(cas.createAnnotation(this.tokenType, 48, 51));
    AnnotationIndex<Annotation> tokenIdx = cas.getAnnotationIndex(this.tokenType);
    
//    AnnotationIndex<AnnotationFS> si = cas.getAnnotationIndex(this.sentenceType);
    for (Annotation sa : ai.select(this.sentenceType)) {
      FSIterator<Annotation> ti2 = tokenIdx.subiterator(sa, false, false);
      
      while (ti2.hasNext()) {
        AnnotationFS t = ti2.next();
        assertTrue("Subiterator returned annotation outside boundaries", t.getBegin() < sa.getEnd());
      }
    }

    SelectFSs<Annotation> ssi = ai.select(this.sentenceType);
    
    for (AnnotationFS sa : ssi) {
      FSIterator<Annotation> ti2 = tokenIdx.select()
          .coveredBy(sa).includeAnnotationsWithEndBeyondBounds(false).nonOverlapping().fsIterator();
      
      while (ti2.hasNext()) {
        AnnotationFS t = ti2.next();
        assertTrue("Subiterator returned annotation outside boundaries", t.getBegin() < sa.getEnd());
      }
    }
  }
  
  /**
   * Test subiterator edge cases
   * 
   *   skip over variations:                           -, i, t1, tn
   *     no match                                             -
   *     match - == id, using == id test                      i
   *     match - != id, using type test,                      t1 or tn
   *                  --  alternative: 1 or multiple to skip over 
   * 
   *   nothing before bound, nothing in bound, nothing after  n n n
   *   nothing before, nothing in bound, stuff after          n n s
   *   nothing before, something in bound, nothing after      n s n    skip over variation
   *   nothing before, something in bound, something after    n s s
   *                                                          
   *   stuff before bound, nothing in bound, nothing after    s n n
   *   stuff before bound, nothing in bound, stuff after      s n s
   *   stuff before, something in bound, nothing after        s s n
   *   stuff before, something in bound, something after      s s s
   *   
   *   test with bound before / after having their begin / end be different or the same
   *     (if the same, have the same or different type;
   *                   if the same type, have the equals-to-bound test be for the same type or same id
   *          
   *       begin end type idtst
   *         d    d   -     -    
   *         d    s   -     -
   *         s    d   -     -
   *         s    s   d     -  test with nnn, nns, nsn, nss, snn, sns, ssn, sss
   *                     p-    test with or without type priority  
   *         s    s   s     n    insure skip over both/multiple
   *         s    s   s     y    insure skip over just 1
   *      
   *   test with type priorities:
   *     skip (only covering)
   *     skipoverbound: if type priority off, can have bound in middle 
   *           
   *   setup notation:  any number of tuples separated by ':'
   *       xxx : yyy : zzz 
   *     each is either - or x-y-t  where x == begin, y == end, t = 0 1 or 2 type order
   */
  private void setupEdges(String s) {
    String [] sa = s.split("\\:");
    for (String x : sa) {
      x = x.trim();
      if ("-".equals(x)) {
        continue;
      }
      String [] i3 = x.split("\\-");
      indexNew(types[Integer.parseInt(i3[2])], Integer.parseInt(i3[0]), Integer.parseInt(i3[1]));
    }
  }
   
  @Test
  public void testEdges() {
    Annotation ba = indexNew(phraseType, 10, 20);  // the bounding annotation
    edge(ba, "-", coveredBy, "--:--:--:--", 0);
    edge(ba, "-", covering, "--:--:--:--", 0);
    edge(ba, "-", sameBeginEnd, "--:--:--:--", 0);
    edge(ba, "-", notBounded, "--:--:--:--", 0);
    
    edge(ba, "0-10-2", coveredBy, "--:--:--:--", 0);
    edge(ba, "0-10-2", covering, "--:--:--:--", 0);

    edge(ba, "0-10-2:11-20-2", coveredBy, "--:--:--:--", 1);
    edge(ba, "0-10-2:11-21-2", coveredBy, "--:--:--:--", 0);  
    edge(ba, "0-10-2:11-21-2", coveredBy, "--:--:LE:--", 1);
  }
  
  /**
   * @param ba -
   * @param setup -
   * @param boundsUse -
   * @param flags TP  type priority
   *               NO  non overlapping
   *               LE  include annotation with ends beyond bounds
   *               ST  skip when same begin end type
   * @param count -
   */
  private void edge(Annotation ba, String setup, BoundsUse boundsUse, String flags, int count) {
    String[] fa = flags.split("\\:");
    cas.reset();
    AnnotationIndex<Annotation> ai = cas.getAnnotationIndex();
    SelectFSs<Annotation> sa;
    
    setupEdges(setup);
    
    switch (boundsUse) {
    case notBounded: sa = ai.select(); break;
    case coveredBy:  sa = ai.select().coveredBy(ba); break;
    case sameBeginEnd: sa = ai.select().at(ba); break;
    default:
    case covering:   sa = ai.select().covering(ba); break;
    }
    
    if (fa[0].equals("TP")) sa.typePriority();
    if (fa[1].equals("NO")) sa.nonOverlapping();
    if (fa[2].equals("LE")) sa.includeAnnotationsWithEndBeyondBounds();
    if (fa[3].equals("ST")) sa.skipWhenSameBeginEndType();
    
    assertEquals(count, sa.fsIterator().size());
  }
  
//  
//  public void testEdges() {
//    
//  }

  private Annotation indexNew(Type type, int begin, int end) {
    Annotation a;
    cas.addFsToIndexes(a = (Annotation) cas.createAnnotation(type, begin, end));
    return a; 
  }
  
  private int setupTheCas() {
    
    
//  Tokens                +---+
//                       +---+
//                      +---+
//  BigBound                      +-----------------------------+
   final String text = "aaaa bbbb cccc dddd aaaa bbbb cccc dddd aaaa bbbb cccc dddd ";
//                      +--------+
//  Sentences                +--------+
//                                +----------+
//  one xtr sent                    +-----------------+  (12, 31)
//
//  Phrases             some overlap, some dont, 3-7 length
//
//  bound4strict                   +------------------+            
//  sentence4strict                 +-----------------------------+
   
   
   try {
     cas.setDocumentText(text);
   } catch (CASRuntimeException e) {
     fail();
   }

   /***************************************************
    * Create and index tokens and sentences 
    ***************************************************/
   FSIndexRepository ir = cas.getIndexRepository();
   int annotCount = 1; // Init with document annotation.
   // create token and sentence annotations
   AnnotationFS fs;
   for (int i = 0; i < text.length() - 5; i++) {
     ++annotCount;
     ir.addFS(fs = cas.createAnnotation(this.tokenType, i, i + 5));
     if (showFSs) {
       System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
     }
   }
   // for (int i = 0; i < text.length() - 5; i++) {
   // cas.getIndexRepository().addFS(cas.createAnnotation(tokenType, i, i+5));
   // }
   
   // create overlapping sentences for unambigious testing
   //   begin =  0,  5, 10, ...
   //   end   = 10, 15, 20, ...
   // non-overlapping:  0-10, 10-20, etc.
   for (int i = 0; i < text.length() - 10; i += 5) {
     ++annotCount;
     ir.addFS(fs = cas.createAnnotation(this.sentenceType, i, i + 10));
     if (showFSs) {
       System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
     }
   }
   
   // create overlapping and non-overlapping phrases
   // begin =  0,  6,  9,  15, 21, 24, 30, 36, ...
   // end   =  5,  9,  16, 20, 24, 31, 35, 39, ...
   
   int beginAlt = 0, endAlt = 0;
   for (int i = 0; i < text.length() - 10; i += 5) {
     ++annotCount;
     ir.addFS(fs = cas.createAnnotation(this.phraseType,  i + beginAlt, i + 5 + endAlt));
     beginAlt = (beginAlt == 1) ? -1 : beginAlt + 1; // sequence: start @ 0, then 1, -1, 0, 1, ...
     endAlt = (endAlt == -1) ? 1 : endAlt - 1; //sequence: start At 0, then -1, 1, 0, -1, ...
     if (showFSs) {
       System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
     }
   }
   
   ++annotCount;
   ir.addFS(fs = cas.createAnnotation(this.sentenceType,  12, 31));
   if (showFSs) {
     System.out.format("creating: %d begin: %d end: %d type: %s%n", annotCount, fs.getBegin(), fs.getEnd(), fs.getType().getName() );
   }
   
   return annotCount;
 }
}
