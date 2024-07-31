package ie.dcu.mcm.hop.pipeline.transforms.taxonomy.internals;

import edu.stanford.nlp.simple.Sentence;

import java.util.List;

import static java.util.Collections.indexOfSubList;

public class SubsetIndexFinder {

    // Method to create the KMP table for a list of strings
    private static int[] createKMPTable(List<String> pattern) {
        int[] kmpTable = new int[pattern.size()];
        int prefixIndex = 0;

        for (int i = 1; i < pattern.size(); i++) {
            while (prefixIndex > 0 && !pattern.get(prefixIndex).equals(pattern.get(i))) {
                prefixIndex = kmpTable[prefixIndex - 1];
            }
            if (pattern.get(prefixIndex).equals(pattern.get(i))) {
                prefixIndex++;
            }
            kmpTable[i] = prefixIndex;
        }
        return kmpTable;
    }

    // Method to find the start index of the subset (pattern) within the larger list (text)
    public static int findSubsetIndex(List<String> largerList, List<String> subset) {
        if (subset.isEmpty()) return 0;
        if (largerList.size() < subset.size()) return -1;

        int[] kmpTable = createKMPTable(subset);
        int j = 0;

        for (int i = 0; i < largerList.size(); i++) {
            while (j > 0 && !largerList.get(i).equals(subset.get(j))) {
                j = kmpTable[j - 1];
            }
            if (largerList.get(i).equals(subset.get(j))) {
                j++;
            }
            if (j == subset.size()) {
                return i - j + 1; // Found the start index of the subset
            }
        }
        return -1; // Subset not found
    }

    public static void main(String[] args) {

        String[] largerArray = {"1", "2", "3", "4he", "5", "6", "3", "4", "5", "7"};
        String[] subsetArray = {"2", "3", "4he"};

        // List<String> largerList = new ArrayList<>(List.of(largerArray));
        //List<String> subset = new ArrayList<>(List.of(subsetArray));

        List<String> largerList = new Sentence(outlier2()).words();
        List<String> subset = new Sentence("well as execute tactically").words();
        //

        // Example: Add elements to the lists
        // largerList.add("example");
        // subset.add("test");
        long a = 0L;
        long b = 0L;
        for (int i = 0; i < 10000; i++) {

            long start = System.nanoTime();
            int index = findSubsetIndex(largerList, subset);
            long end = System.nanoTime() - start;
            a += end;
            if (index != -1) {
                System.out.println(end + " a.Subset starts at index: " + index);
            } else {
                System.out.println(end + " a.Subset not found");
            }

            start = System.nanoTime();
            index = indexOfSubList(largerList, subset);
            end = System.nanoTime() - start;
            if (index != -1) {
                System.out.println(end + " b.Subset starts at index: " + index);
            } else {
                System.out.println(end + " b.Subset not found");
            }
            b += end;
        }

        System.out.println(a + "");
        System.out.println(b + "");
        System.out.println((b - a) + "");
    }

    public static String outlier2() {
        return "About GGA - Our mission is advancing yours GGA is a full-service philanthropic management consulting firm with 60 years of industry-defining leadership in helping our clients realize the full potential of their missions through the growth of philanthropic support. Our primary mission is to assist our clients in the substantial improvement and acceleration of their fundraising programs. We seek to align those programs with the institutionâ€™s core mission and ensure their sustainability. GGA is an international resource for data-driven best practices in all areas of advancement, including fundraising, engagement, communications, and advancement services. We provide counsel to a global constituency of nonprofits, specifically in the areas of higher education, academic medicine/healthcare, independent schools, arts, and culture, select humanitarian institutions, and a range of charities and nonprofit organizations. GGA embodies a high-performance and entrepreneurial culture. We are client-centric, rigorous, creative, and data-driven in our practices, and committed to responding swiftly and effectively to client needs and requirements. We offer a challenging environment and seek intelligent, energetic, and self-motivated individuals who are achievement-oriented and have high integrity. Position Summary The firm seeks an entrepreneurial and energetic advancement professional to fill the position of Vice President for the Higher Education practice area. This person will have a minimum of 10 years frontline development experience preferably in public university and liberal arts college environments. The optimal candidate will bring prior experience managing fundraising programs in campaign environments. The Vice President will receive a high level of support from the firmâ€™s corporate headquarters in Chicago. Our Consulting Resources Team provides contract administration, client services, knowledge management, and marketing. All GGA consultants deploy evidenced-based consulting methodologies and develop sophisticated analytics tools in close collaboration with the firm's integrated, large-scale client engagement teams. A Vice President may function as a member of various project teams and/or be assigned as lead a Project Director, leading data collection, performing rigorous analysis, and producing clear, compelling, insightful, and accurate reports for the firmâ€™s clients. The Vice President will also collaborate with colleagues and contribute to innovative product/service development to drive sustained growth and profitability, while ensuring that client deliverables meet the highest standards of quality. Vice Presidents are self-starters with strong leadership skills. They possess business and entrepreneurial acumen with demonstrated ability to initiate and sustain client relationships. It is essential that our Vice Presidents exhibit a high level of flexibility, perseverance, and a tenacious work ethic. Key Responsibilities To perform this job successfully, an individual must be able to perform each essential duty satisfactorily. The requirements listed below are representative of the knowledge, skill and/or ability required. Reasonable accommodations may be made to enable individuals with disabilities to perform the essential functions. Other duties may be assigned. Core Mission Contribute to the GGA Higher Education practice area by developing strategies, products and capabilities, marketing and sales plans, and delivery methodologies that enhance the ability to initiate, sustain and steward the philanthropic missions of our clients. Leadership Leverage a wide range of skills, experience, and leadership approaches to effectively collaborate with a group of talented, creative consulting resources professionals, who will build upon existing programs and create new programs and client offerings Ensure a focus on strategic and operational leadership and management, ensuring quality of deliverables against exacting deadlines Motivate others in the firm to identify and implement specific integrated projects intended to enhance the visibility of the group in their competitive arena and develop a dominant position in marketplace Optimize value and service delivery to clients, increase and secure profitable business, and motivate and develop a high-performing team Consulting Provide consulting services to client institutions which might including fundraising program assessments, strategic planning (feasibility) studies, campaign planning, major gift program planning and execution, staff and volunteer training, strategic planning, alumni relations assessments, case for support development, etc. Liaise with client organization staff and leaders with respect and confidence, including senior leadership across and beyond advancement functions Demonstrate strong commitment to working as part of a team on client engagements across a spectrum of non-profit institutions that vary by size, mission, and sector Compile and present clear, compelling, cohesive, accurate, and persuasive presentations and reports, independently or with the support of others. Such compilation and review usually includes: reconciliation of anecdotal findings, benchmarking data, Major Gifts Portfolio Analysis (MGPA), and other analytics (if included) to ensure alignment throughout the report/presentation Operations Ensure that practice area products and services support GGAâ€™s overall strategy and revenue goals Grow and guide support of the widening range of products, services, and client deliverables Collaborate with GGA Consulting Team in process improvement and management methodologies to increase efficiencies, cost effectiveness, and workflow optimization Contribute to continuous improvement in all areas including systems, policies and procedures, client service and technical support, quality control and management reporting in order to improve the overall effectiveness of the practice area Business and Product Development Create, design, and implement strategies to drive revenue growth and profitability Share strategic business development and planning ideas, including new product development and ongoing innovation of existing products Positively and proactively support the drive for teamwork, cooperation, and results-oriented outcomes with consulting professionals across the firm Collaborate with the Business Development Team to develop, review/edit proposals for consulting services to prospective clients and review/edit proposals prepared by other teams Lead and/or participate in sales presentations as appropriate Pursue opportunities to support the overall marketing/business development objectives of product portfolio by creating lead-generating content, articles, webinars, speaking at professional conferences, writing white papers, and/or other similar activities Education, Experience and Attributes Bachelorâ€™s degree required; advanced degree preferred Minimum of ten (10) years of frontline development experience with exposure to major campaign planning and leading high performing fundraising teams Prior experience managing fundraising programs in campaign environments Prefer direct experience from public university (preferably with a state flagship institution) and/or Liberal arts college Demonstrate strategic thinking applied to a rigorous, analytic approach to client situations, tailoring delivery to a clientâ€™s specific culture or need Demonstrate understanding of fundraising best practices and the non-profit sector Demonstrate broad insight of philanthropic management Ability to take a hands-on approach to consulting with the ultimate goal of providing an individualized, long-term, sustainable solution for each client Client-focused with an intense intellectual curiosity about each individual organization and what makes it unique Demonstrate a high level of flexibility, stamina and perseverance with an unbridled passion and commitment to the field of philanthropy, as well as a strong and tenacious work ethic Experience with Salesforce Customer Relationship Management (CRM) and SharePoint Demonstrate proficiency with Microsoft Office tools, specifically with Word, PowerPoint, Outlook, and Excel Ability to travel up 3-4 days per week, as needed Communication and Relationship Skills Ability to establish, cultivate and nurture high value relationships with clients, vendors, and internal stakeholders at all levels Ability to influence leadership by providing data-driven recommendations that show clients the â€œwhyâ€ as well as the â€œhowâ€ Demonstrate judgment and poise to present findings and recommendations in a confident, inspiring manner Demonstrate discretion when dealing with highly confidential issues Demonstrate effective emotional intelligence and intuition necessary to appropriately engage with multiple personalities in a variety of client environments Professional and polished executive presence and business acumen; verbal/written communication skills Demonstrated ability to effectively collaborate with consulting professionals and clients Strong sales skills and a significant drive to build a client base in the higher education practice area Poise and maturity to present findings and recommendations in a confident, inspiring manner Ability to produce top-quality work under pressure, with minimal direction, while dealing with competing priorities and exacting deadlines Ability to provide highly valued client-service for both internal and external clients, with focus on continual improvement Strong interpersonal skills and ability to motivate teams and individuals Ability to read, write, speak and comprehend English fluently Reasoning Ability Critical thinking that enables effective data review and ability to generate narrative observations, findings and recommendations Ability to think strategically (see big picture) as well as execute tactically (know the details) Ability to use diplomacy and tact when dealing with problems Ability to anticipate problems and determine the best course of action for their resolution Ability to anticipate possible risks and develop a mitigation plan in preparation Demonstrated organizational and project management skills, including the ability to manage multiple projects and competing priorities in a fast-paced, deadline-driven environment Demonstrates sound discretion and professional judgement in decision making Physical and Travel Requirements The physical requirements described here are representative of those that must be met by an employee to successfully perform the essential functions of this job. Reasonable accommodations may be made to enable individuals with disabilities to perform the essential functions. Ability to use office equipment such as computer, keyboard, mouse technology Repetitive motion activities such as heavy typing and using a mouse Ability to adjust focus quickly from one medium to another Flexible hours may be required to accommodate global clients Client related travel could be as much as 3 to 4 days/week GGA has a strong work culture, evidenced by the deep commitment and extraordinary work ethic of its staff. Our work ethic and culture are established and reinforced by key leadership roles such as this. GGA is proud to be an Equal Opportunity Employer, committed to creating a diverse and inclusive workforce. We consider all qualified applicants without regard to race, religion, color, sex, national origin, age, sexual orientation, gender identity, disability, veteran status or other legally protected classifications. Powered by JazzHR";
    }

}
