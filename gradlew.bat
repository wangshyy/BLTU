ΚώΊΎ   3y groovy/util/XmlSlurper  "org/xml/sax/helpers/DefaultHandler  XmlSlurper.java groovy/util/XmlSlurper$1  reader Lorg/xml/sax/XMLReader; currentNode !Lgroovy/util/slurpersupport/Node; stack Ljava/util/Stack; 4Ljava/util/Stack<Lgroovy/util/slurpersupport/Node;>; 
charBuffer Ljava/lang/StringBuilder; namespaceTagHints Ljava/util/Map; 5Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>; keepIgnorableWhitespace Z namespaceAware <init> ()V .javax/xml/parsers/ParserConfigurationException  org/xml/sax/SAXException  (ZZ)V  
   this Lgroovy/util/XmlSlurper; (ZZZ)V  "
  # 
validating  
  & 
 	  ( java/util/Stack *
 + &  	  - java/lang/StringBuilder /
 0 &  	  2 java/util/HashMap 4
 5 &  	  7  	  9  	  ; groovy/xml/FactorySupport = createSaxParserFactory &()Ljavax/xml/parsers/SAXParserFactory; ? @
 > A "javax/xml/parsers/SAXParserFactory C setNamespaceAware (Z)V E F
 D G setValidating I F
 D J 7http://javax.xml.XMLConstants/feature/secure-processing L 
setQuietly :(Ljavax/xml/parsers/SAXParserFactory;Ljava/lang/String;Z)V N O
  P 4http://apache.org/xml/features/disallow-doctype-decl R java/lang/String T newSAXParser ()Ljavax/xml/parsers/SAXParser; V W
 D X javax/xml/parsers/SAXParser Z getXMLReader ()Lorg/xml/sax/XMLReader; \ ]
 [ ^  		  ` allowDocTypeDeclaration factory $Ljavax/xml/parsers/SAXParserFactory; (Lorg/xml/sax/XMLReader;)V  (Ljavax/xml/parsers/SAXParser;)V  e
  g parser Ljavax/xml/parsers/SAXParser; $org/xml/sax/SAXNotSupportedException k %org/xml/sax/SAXNotRecognizedException m 
setFeature (Ljava/lang/String;Z)V o p
 D q java/lang/Exception s feature Ljava/lang/String; value setKeepWhitespace Ljava/lang/Deprecated; setKeepIgnorableWhitespace z F
  { keepWhitespace isKeepIgnorableWhitespace ()Z getDocument *()Lgroovy/util/slurpersupport/GPathResult; xml  $http://www.w3.org/XML/1998/namespace  java/util/Map  put 8(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;     $groovy/util/slurpersupport/NodeChild  [(Lgroovy/util/slurpersupport/Node;Lgroovy/util/slurpersupport/GPathResult;Ljava/util/Map;)V  
   java/lang/Throwable  parse C(Lorg/xml/sax/InputSource;)Lgroovy/util/slurpersupport/GPathResult; java/io/IOException  org/xml/sax/XMLReader  setContentHandler (Lorg/xml/sax/ContentHandler;)V     (Lorg/xml/sax/InputSource;)V      
    input Lorg/xml/sax/InputSource; 8(Ljava/io/File;)Lgroovy/util/slurpersupport/GPathResult; java/io/FileInputStream ₯ (Ljava/io/File;)V  §
 ¦ ¨ org/xml/sax/InputSource ͺ (Ljava/io/InputStream;)V  ¬
