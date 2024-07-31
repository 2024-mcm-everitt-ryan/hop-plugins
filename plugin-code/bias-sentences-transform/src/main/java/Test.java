import com.google.gson.Gson;
import com.squareup.moshi.Moshi;
import edu.stanford.nlp.coref.docreader.CoNLLDocumentReader;
import edu.stanford.nlp.ling.BasicDocument;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.DocumentReader;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.collections4.ListValuedMap;
import ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.TaxonomyTermAnnotation;

import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.Collections.indexOfSubList;
import static org.apache.commons.collections4.MultiMapUtils.newListValuedHashMap;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static ie.dcu.mcm.hop.pipeline.transforms.bias.sentences.TaxonomyTermAnnotation.Builder.biasTermBuilder;

public class Test {

    public static void main(String[] args) {
        String sample = "To accomplish this task efficiently and successfully, he must possess a digital native's bravado, coupled with a strong ability to navigate our ATS.";


        new Document(sample).sentences().forEach(sentence -> {
            var pos = sentence.posTags();
            var toks = sentence.words();

            for (int i = 0; i < toks.size(); i++) {
                out.print(String.format("%s(%s) ", toks.get(i), pos.get(i)));
            }
        });

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        //props.put("annotators", "tokenize, ssplit, parse, lemma, ner, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String text = "Second healthcare worker in Texas tests positive for Ebola , authorities say ."; // Add your text here!
        Annotation document = new Annotation(sample);
        pipeline.annotate(document);
        String[] myStringArray = {"SentencesAnnotation"};
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            IndexedWord root = dependencies.getFirstRoot();
            System.out.printf("%nroot(ROOT-0, %s-%d)%n", root.word(), root.index());
            for (SemanticGraphEdge e : dependencies.edgeIterable()) {
                System.out.printf ("%s(%s-%d, %s-%d)%n", e.getRelation().getLongName(), e.getGovernor().word(), e.getGovernor().index(), e.getDependent().word(), e.getDependent().index());
            }
        }
    }

    public static void main2(String[] args) {

        final HeadFinder chf = new CollinsHeadFinder();

        Document doc = new Document(sample1());
        Map<String, List<String>> map = new HashMap<>();
        map.put("masculine", List.of("Training & Development", "under pressure", "self-starter", "he"));
        map.put("feminine", List.of("communication skills", "coaching", "she"));
        map.put("general", List.of("implement"));
        ListValuedMap<String, TaxonomyTermAnnotation> multimap = newListValuedHashMap();

        // this.nThreads = PropertiesUtils.getInt(props, annotatorName + ".nthreads", PropertiesUtils.getInt(props, "nthreads", 1));
        //
        for (Sentence sentence : doc.sentences()) {
            String text = trim(sentence.text());
            if (isBlank(text)) {
                continue;
            }

            List<String> words = sentence.words();
            if (words.isEmpty()) {
                continue;
            }
            List<String> ner = null;
            List<String> pos = null;
            for (Map.Entry<String, List<String>> e : map.entrySet()) {
                for (String v : e.getValue()) {
                    v = trim(v);
                    if (isBlank(v)) {
                        continue;
                    }

                    List<String> term = new Sentence(v).words();
                    if (term.isEmpty()) {
                        continue;
                    }


                    int startIndex = indexOfSubList(words, term);
                    if (startIndex == -1) {
                        // Term is not a subset of words
                        continue;
                    }
                    if (pos == null) {
                        pos = sentence.posTags();
                    }

                    int endIndex = startIndex + term.size();
                    TaxonomyTermAnnotation b = biasTermBuilder()
                            .startIdx(startIndex)
                            .endIdx(endIndex)
                            .term(v)
                            .tokenCount(term.size())
                            .tokens(term)
                            .pos(pos.subList(startIndex, endIndex))
                            .build();

                    multimap.put(e.getKey(), b);
                }
            }
        }

        for (Map.Entry<String, Collection<TaxonomyTermAnnotation>> e : multimap.asMap().entrySet()) {
            out.println(e.getKey());
            for (TaxonomyTermAnnotation b : e.getValue()) {
                out.println("\t" + b);
            }
        }

    }

    public static void benchmark(String[] args) {
        Document doc = new Document(sample2());
        List<Map<String, List<String>>> maps = new ArrayList<>();
        for (Sentence sentence : doc.sentences()) {
            out.println(sentence.text());
            out.println(sentence.tokens());
            out.println(sentence.posTags());

            Map<String, List<String>> map = new HashMap<>();
            //    map.put("sentence",List.of(sentence.text()));
            map.put("tokens", sentence.words());
            map.put("pos", sentence.posTags());
            //  map.put("ner",sentence.nerTags());
            maps.add(map);
        }

        Moshi moshi = new Moshi.Builder().build();
        //  JsonAdapter<TaxonomyTerms> jsonAdapter = moshi.adapter(TaxonomyTerms.class);
        int m = 0;
        int g = 0;
        for (int j = 0; j < 10; j++) {
            long start = currentTimeMillis();
            for (int i = 0; i < 1; i++) {
                //   out.println(jsonAdapter.toJson(maps));
            }
            long tm = currentTimeMillis() - start;

            start = currentTimeMillis();
            Gson gson = new Gson();
            for (int i = 0; i < 1; i++) {
                out.println(gson.toJson(maps));
            }
            long tg = currentTimeMillis() - start;

            out.println("moshi: " + tm);
            out.println("gson: " + tg);

            if (tm < tg) {
                m++;
            } else if (tm == tg) {
                m++;
                g++;
            } else if (tm > tg) {
                g++;
            }
        }


        out.println("total moshi: " + m);
        out.println("total gson: " + g);

    }


    public static String sample1() {
        return "If you are the new team member we’re looking for you to be… A highly organized self-starter who has a desire for excellence with an exceptional ability to learn and implement systems and processes. A resourceful researcher who doesn’t take no for an answer, continually researching other avenues to ensure that we deliver exceptional customer satisfaction on time every time. A passionate team player who brings out the best in people, helping them to deliver our promise. You will have a strong plant knowledge with excellent organisational ability. You will be responsible for the smooth running of garden build projects and planting activities....while being people focused with a positive can do attitude…. You continually look to improve how things get done to deliver exceptional results with a customer care focus. If this sounds like you and you are ready to join Outlook Gardens, Ireland’s leading, multi award winning garden design, landscape construction and maintenance company, then submit your C.V. by Friday the 9th of October with a cover email describing why you feel you would thrive in this role. Salary DOE Reference ID: Landscape Foreman Job Types: Full-time, Permanent Salary: €35,000.00-€55,000.00 per year Experience: foreman: 3 years (Required) Landscape Foreman: 1 year (Preferred) Landscaping: 1 year (Preferred) Licence: Excavator (Preferred) Work remotely: No";
    }

    public static String sample2() {
        return "The Spanish Localization QA Tester will analyze in-game texts to validate grammar, syntax, spelling and proper location on a variety of different platforms to ensure compliance with all console and handheld manufacturers localized official terminologies. In addition, he/she will also review and perform exhaustive Spanish language tests to ensure text and audio respect the cultural aspects of a game’s local edition remains in context and is consistent throughout. The Spanish Localization QA Tester is responsible for testing, identifying, recording and suggesting fixes to potential bugs in video and computer games relating to language, implementation, user interface (UI) and compliance issues. Please note: This role will be remote and in order to be eligible you must be located in Dublin or surrounding areas as specific equipment will be delivered to new employees. Verify linguistic accuracy in the Spanish version of the in-game text and check for typographical, grammatical and punctuation errors. Verify cosmetic integrity of in-game text and ensure that all text is correctly displayed. Verify the contextual consistency of in-game text. Assess cultural appropriateness and check politically sensitive content. Verify the correct implementation of the localized text. Ensure that the correct approved terminology is used throughout the game. Verify the correct functionality, within the localized version of the game. Verify testing results across all languages in test. Maintain high level of focus throughout the project. Identify, isolate, and document bugs clearly and concisely in a bug database. Run test-cases and checklists. Follow the documented processes at all stages of the testing process. Verify that bugs have been fixed and implemented correctly. File review/Correcting files: Proof-read and correct translations. Ensure that quality, accuracy and consistency is maintained throughout files/game/manual. Translation work: Occasionally provide Spanish in-house translations. Attend meetings with testing team, team lead and other interested parties, on a regular basis. Communicate through the appropriate channels. Provide daily status reports about the status of the project testing. Support lead by taking ownership of side tasks, such as daily report, bug vetting, coaching of new team members. Requirements Excellent spelling and grammar in Spanish language. Outstanding attention to detail. Must be motivated and able to remain focused until project completion. Reliable and punctual. Excellent written, verbal and interpersonal communication skills in both English and Spanish. The ability to write accurate, unambiguous and concise documentation in English and your native language. Good PC knowledge and proficient knowledge of MS Office products is essential. Must be a team worker. Experience of Software Localization as a QA Tester is a definite advantage. University Degree, equivalent qualification and/or relevant experience in related field an advantage. Competent games player. Basic understanding of the development cycle and constraints. Flexible towards responsibilities and working hours. Ability to cope under pressure and to work to tight deadlines. Benefits Training & Development";
    }

    public static String outlier1() {
        return "Reporting to the CEO, the Head of Human Resources will guide the development of the organization through its planned growth, cultivate a development-orientated culture heavily rooted in the Company’s values (Competence, Commitment, Connection, Communication), create and enhance HR programs, guidelines and tools to deliver the highest level of candidate and employee experience. With a strong balance of business acumen and deep HR knowledge and experience, the Head of HR will ensure HR services are flawlessly delivered in an accurate and timely manner. The Head of HR is a member of the Executive Leadership Team (CEO’s direct reports), Senior Management Team (~n-1), manages a very high-performing team of two FTEs and two contract HR team members as well as a variety of external vendors and resources. The role is based at the Company’s SSF headquarters and is required onsite most days (flexible over the coming months due to the pandemic). About Catalyst Biosciences, the Protease Medicines company Catalyst is a research and clinical development biopharmaceutical company focused on addressing unmet medical needs in rare disorders of the complement and coagulation systems. Our protease engineering platform has generated two late-stage clinical programs, including MarzAA, a subcutaneously (SQ) administered next-generation engineered coagulation Factor VIIa (FVIIa) for the treatment of episodic bleeding in subjects with rare bleeding disorders. Our complement pipeline includes a pre-clinical C3-degrader program partnered with Biogen for dry age-related macular degeneration, an improved complement factor I protease for SQ replacement therapy in patients with CFI deficiency and C4b-degraders designed to target disorders of the classical complement pathway as well as other complement programs in development. Responsibilities Drive business results by creating and implementing best practice human resource initiatives tailored to business requirements and legal guidelines Serve as thought partner, executive coach, advisor to CEO and executive team to ensure the people and organization strategy enables achieving the business results Enhance and execute a Talent Acquisition strategy, including identifying resources to meet the Company’s hiring needs Champion an inclusive and diverse workforce, implementing programs to ensure the business develops and retains high quality talent at all levels in a continuous pipeline to support the growth objectives, opportunities and strategies of the business Enhance and manage the compensation infrastructure for executive and non-executive cash and equity compensation Act as coach/mentor/advocate for employees and managers; resolve employee relations issues and develop proactive solutions based on root-cause trend analysis Identify and implement hard and soft skill training; ensure compliance training is delivered and monitored on timely basis Oversee the HR function and manage the HR team Requirements 15 years or more of progressively increasing responsibilities and experience within Human Resources Prior experience scaling up a biotech or pharma company Credible and savvy businessperson with history of quickly building credibility & trust with executives, leaders, and professionals; reputation for unquestioned personal and business integrity Knowledgeable in leading edge HR practices & trends; a strong leader who influences change and transformation Mission-driven individual with high energy, agility, and strong levels of perseverance; successful in fast-paced, dynamic, collaborative environments Reputation for operating strategically and tactically; willing to do what it takes to drive results; strong attention to detail, not hindered by protocol or process or bureaucracy Excellent verbal and written communication, interpersonal and influencing skills up, down and across the organization Record of working independently without supervision; demonstration of exercising excellent judgment/decision making, at times without complete information History of success working collaboratively across the organization and with diverse partners achieving corporate objectives Bachelor’s degree in industrial/organizational psychology, human resource management, business or a related field Proficiency in Microsoft office **Title/Level will commensurate with years of experience and education In terms of the performance and personal competencies required for the position, we would highlight the following: Setting Strategy The ability to create and articulate an inspiring vision for the organization, not only for the areas s/he is directly responsible for, but the Company as a whole The inclination to seek and analyze data from a variety of sources to support decisions and to align others with the organization’s overall strategy An entrepreneurial and creative approach to developing new, innovative ideas that will stretch the organization and push the boundaries within the industry The ability to effectively balance the desire and need for broad change with an understanding of how much change the organization is capable of handling, to create realistic goals and implementation plans that are achievable and successful Executing for Results The ability to set clear and challenging goals while committing the organization to improved performance; tenacious and accountable in driving results Comfortable with ambiguity and uncertainty; the ability to adapt nimbly and lead others through complex situations A risk-taker who seeks data and input from others to foresee possible threats or unintended circumstances from decisions; someone who takes smart risks A leader who is viewed by others as having a high degree of integrity and forethought in his/her approach to making decisions; the ability to act in a transparent and consistent manner while always taking into account what is best for the organization Leading Teams The ability to attract and recruit top talent, motivate the team, delegate effectively, celebrate diversity, and manage performance; widely viewed as a strong developer of others The ability to persevere in the face of challenges and exhibit a steadfast resolve and relentless commitment to higher standards, which commands respect from followers A leader who is self-reflective and aware of his/her own limitations; leads by example and drives the organization’s performance with an attitude of continuous improvement by being open to feedback and self-improvement Relationships and Influence Naturally connects and builds strong relationships with others, demonstrating strong emotional intelligence and an ability to communicate clearly and persuasively An ability to inspire trust and followership in others through compelling influence, powerful charisma, passion in his/her beliefs, and active drive Encourages others to share the spotlight and visibly celebrates and supports the success of the team Creates a sense of purpose/meaning for the team that generates followership beyond his/her own personality and engages others to the greater purpose for the organization as a whole Driving Sustainability Firmly believes that both sustainability and profit are in the organization’s best long-term interest Integrates economic, societal and environmental factors into a purpose-driven strategy, turning sustainability into a competitive advantage Understands and incorporates viewpoints from all key stakeholders to drive decision making and share the benefits Delivers breakthrough innovations and business models that create value for all stakeholders, continually challenging traditional approaches Sets audacious business and sustainability goals, driving concerted action and investments and stays the course in the face of setbacks or push-back from short-term oriented stakeholders";
    }

    public static String outlier2() {
        return "About GGA - Our mission is advancing yours GGA is a full-service philanthropic management consulting firm with 60 years of industry-defining leadership in helping our clients realize the full potential of their missions through the growth of philanthropic support. Our primary mission is to assist our clients in the substantial improvement and acceleration of their fundraising programs. We seek to align those programs with the institutionâ€™s core mission and ensure their sustainability. GGA is an international resource for data-driven best practices in all areas of advancement, including fundraising, engagement, communications, and advancement services. We provide counsel to a global constituency of nonprofits, specifically in the areas of higher education, academic medicine/healthcare, independent schools, arts, and culture, select humanitarian institutions, and a range of charities and nonprofit organizations. GGA embodies a high-performance and entrepreneurial culture. We are client-centric, rigorous, creative, and data-driven in our practices, and committed to responding swiftly and effectively to client needs and requirements. We offer a challenging environment and seek intelligent, energetic, and self-motivated individuals who are achievement-oriented and have high integrity. Position Summary The firm seeks an entrepreneurial and energetic advancement professional to fill the position of Vice President for the Higher Education practice area. This person will have a minimum of 10 years frontline development experience preferably in public university and liberal arts college environments. The optimal candidate will bring prior experience managing fundraising programs in campaign environments. The Vice President will receive a high level of support from the firmâ€™s corporate headquarters in Chicago. Our Consulting Resources Team provides contract administration, client services, knowledge management, and marketing. All GGA consultants deploy evidenced-based consulting methodologies and develop sophisticated analytics tools in close collaboration with the firm's integrated, large-scale client engagement teams. A Vice President may function as a member of various project teams and/or be assigned as lead a Project Director, leading data collection, performing rigorous analysis, and producing clear, compelling, insightful, and accurate reports for the firmâ€™s clients. The Vice President will also collaborate with colleagues and contribute to innovative product/service development to drive sustained growth and profitability, while ensuring that client deliverables meet the highest standards of quality. Vice Presidents are self-starters with strong leadership skills. They possess business and entrepreneurial acumen with demonstrated ability to initiate and sustain client relationships. It is essential that our Vice Presidents exhibit a high level of flexibility, perseverance, and a tenacious work ethic. Key Responsibilities To perform this job successfully, an individual must be able to perform each essential duty satisfactorily. The requirements listed below are representative of the knowledge, skill and/or ability required. Reasonable accommodations may be made to enable individuals with disabilities to perform the essential functions. Other duties may be assigned. Core Mission Contribute to the GGA Higher Education practice area by developing strategies, products and capabilities, marketing and sales plans, and delivery methodologies that enhance the ability to initiate, sustain and steward the philanthropic missions of our clients. Leadership Leverage a wide range of skills, experience, and leadership approaches to effectively collaborate with a group of talented, creative consulting resources professionals, who will build upon existing programs and create new programs and client offerings Ensure a focus on strategic and operational leadership and management, ensuring quality of deliverables against exacting deadlines Motivate others in the firm to identify and implement specific integrated projects intended to enhance the visibility of the group in their competitive arena and develop a dominant position in marketplace Optimize value and service delivery to clients, increase and secure profitable business, and motivate and develop a high-performing team Consulting Provide consulting services to client institutions which might including fundraising program assessments, strategic planning (feasibility) studies, campaign planning, major gift program planning and execution, staff and volunteer training, strategic planning, alumni relations assessments, case for support development, etc. Liaise with client organization staff and leaders with respect and confidence, including senior leadership across and beyond advancement functions Demonstrate strong commitment to working as part of a team on client engagements across a spectrum of non-profit institutions that vary by size, mission, and sector Compile and present clear, compelling, cohesive, accurate, and persuasive presentations and reports, independently or with the support of others. Such compilation and review usually includes: reconciliation of anecdotal findings, benchmarking data, Major Gifts Portfolio Analysis (MGPA), and other analytics (if included) to ensure alignment throughout the report/presentation Operations Ensure that practice area products and services support GGAâ€™s overall strategy and revenue goals Grow and guide support of the widening range of products, services, and client deliverables Collaborate with GGA Consulting Team in process improvement and management methodologies to increase efficiencies, cost effectiveness, and workflow optimization Contribute to continuous improvement in all areas including systems, policies and procedures, client service and technical support, quality control and management reporting in order to improve the overall effectiveness of the practice area Business and Product Development Create, design, and implement strategies to drive revenue growth and profitability Share strategic business development and planning ideas, including new product development and ongoing innovation of existing products Positively and proactively support the drive for teamwork, cooperation, and results-oriented outcomes with consulting professionals across the firm Collaborate with the Business Development Team to develop, review/edit proposals for consulting services to prospective clients and review/edit proposals prepared by other teams Lead and/or participate in sales presentations as appropriate Pursue opportunities to support the overall marketing/business development objectives of product portfolio by creating lead-generating content, articles, webinars, speaking at professional conferences, writing white papers, and/or other similar activities Education, Experience and Attributes Bachelorâ€™s degree required; advanced degree preferred Minimum of ten (10) years of frontline development experience with exposure to major campaign planning and leading high performing fundraising teams Prior experience managing fundraising programs in campaign environments Prefer direct experience from public university (preferably with a state flagship institution) and/or Liberal arts college Demonstrate strategic thinking applied to a rigorous, analytic approach to client situations, tailoring delivery to a clientâ€™s specific culture or need Demonstrate understanding of fundraising best practices and the non-profit sector Demonstrate broad insight of philanthropic management Ability to take a hands-on approach to consulting with the ultimate goal of providing an individualized, long-term, sustainable solution for each client Client-focused with an intense intellectual curiosity about each individual organization and what makes it unique Demonstrate a high level of flexibility, stamina and perseverance with an unbridled passion and commitment to the field of philanthropy, as well as a strong and tenacious work ethic Experience with Salesforce Customer Relationship Management (CRM) and SharePoint Demonstrate proficiency with Microsoft Office tools, specifically with Word, PowerPoint, Outlook, and Excel Ability to travel up 3-4 days per week, as needed Communication and Relationship Skills Ability to establish, cultivate and nurture high value relationships with clients, vendors, and internal stakeholders at all levels Ability to influence leadership by providing data-driven recommendations that show clients the â€œwhyâ€ as well as the â€œhowâ€ Demonstrate judgment and poise to present findings and recommendations in a confident, inspiring manner Demonstrate discretion when dealing with highly confidential issues Demonstrate effective emotional intelligence and intuition necessary to appropriately engage with multiple personalities in a variety of client environments Professional and polished executive presence and business acumen; verbal/written communication skills Demonstrated ability to effectively collaborate with consulting professionals and clients Strong sales skills and a significant drive to build a client base in the higher education practice area Poise and maturity to present findings and recommendations in a confident, inspiring manner Ability to produce top-quality work under pressure, with minimal direction, while dealing with competing priorities and exacting deadlines Ability to provide highly valued client-service for both internal and external clients, with focus on continual improvement Strong interpersonal skills and ability to motivate teams and individuals Ability to read, write, speak and comprehend English fluently Reasoning Ability Critical thinking that enables effective data review and ability to generate narrative observations, findings and recommendations Ability to think strategically (see big picture) as well as execute tactically (know the details) Ability to use diplomacy and tact when dealing with problems Ability to anticipate problems and determine the best course of action for their resolution Ability to anticipate possible risks and develop a mitigation plan in preparation Demonstrated organizational and project management skills, including the ability to manage multiple projects and competing priorities in a fast-paced, deadline-driven environment Demonstrates sound discretion and professional judgement in decision making Physical and Travel Requirements The physical requirements described here are representative of those that must be met by an employee to successfully perform the essential functions of this job. Reasonable accommodations may be made to enable individuals with disabilities to perform the essential functions. Ability to use office equipment such as computer, keyboard, mouse technology Repetitive motion activities such as heavy typing and using a mouse Ability to adjust focus quickly from one medium to another Flexible hours may be required to accommodate global clients Client related travel could be as much as 3 to 4 days/week GGA has a strong work culture, evidenced by the deep commitment and extraordinary work ethic of its staff. Our work ethic and culture are established and reinforced by key leadership roles such as this. GGA is proud to be an Equal Opportunity Employer, committed to creating a diverse and inclusive workforce. We consider all qualified applicants without regard to race, religion, color, sex, national origin, age, sexual orientation, gender identity, disability, veteran status or other legally protected classifications. Powered by JazzHR";
    }
}
