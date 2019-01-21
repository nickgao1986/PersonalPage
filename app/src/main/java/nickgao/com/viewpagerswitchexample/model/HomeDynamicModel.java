package nickgao.com.viewpagerswitchexample.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nickgao.com.viewpagerswitchexample.Util.StringUtils;

public class HomeDynamicModel implements Serializable {
    private static final long serialVersionUID = 165165416L;
    private static final String TAG = "HomeDynamicModel";
    public String uuid = "";
    public int id; //未发送成功 id=0
    public int type = 1;
    public boolean isSendFail;
    public int originalId;

    public int userId;
    public String screen_name;      //发布者昵称
    public String created_time; //创建时间
    public String updated_date; ////最后评论时间，无评论则没有此字段
    public int sort;
    public boolean allow_operate;//是否允许操作回复
    public int isvip = 0; //是否是认证用户0:未认证  1:认证
    public int user_type;


    public int praise_num = 0; //赞的数量
    public int isPraise = 0; //是否已经点赞1为点赞  0  未点赞
    public int comment_num;  //评论数量

    public String title;
    public String content; //内容

    public String shareWords;//分享感言 TOPIC_SHARE_TYPE
    public String publisher; //原帖子发布者

    //type = 3 圈子推荐
    public String iconUrl;

    public String typeIcon;
    public String avatUrl;//推广的icon，优先级最高
    public String circleName;
    public int forumId;

    public String idUrl; //web页面是url地址，其它为id
    //贴士
    public String tipUrl;//分享的贴士url
    public String tipCategory; //分类标题
    public int tipCategoryId;//分类id
    public int tipId; //贴士id
    public int topicId; //贴士转话题后的id

    public int isMoreComment = 0; //是否有更多评论
    public List<String> imagesList;//图片列表


    public List<String> localImageThumbList;
    public String verify_str = "";
    public Integer sortPosition = 0; //排序位置
    public int isExpand = 0;//  是否为推广字段， 1 为是 ， 0 为不是

    public boolean bTextExpand = false;
    public String recommType = ""; //推广类型

    public int isRecommend = 0; // 是否是推荐说说 1:是，0不是
    public String related; //推荐原由，如:"她他圈聊过的好友",
    public int dynamicNum;
    public int fans;
    public int isFollow = 0; // 是否已关注推荐好友
    public int goHomepage;
    public String info; // 圈子小助手推荐提示
    public int isPublic;

    public String toolUrl;

    /**
     * 如果type=13，news_type为资讯类型
     */
    public int news_type;

    /**
     * 跳转协议
     */
    public String redirect_url;

    /**
     * 视频的时长
     */
    public String video_time;

    public TopicAvatarModel avatarModel;


    public HomeDynamicModel() {
    }



    public HomeDynamicModel(JSONObject jsonObject) {

        id = StringUtils.getJsonInt(jsonObject, "id");
        //uuid = StringUtils.getJsonString(jsonObject,"verify_original");
        verify_str = StringUtils.getJsonString(jsonObject, "verify_original");
        type = StringUtils.getJsonInt(jsonObject, "type");
        news_type = StringUtils.getJsonInt(jsonObject, "news_type");
        redirect_url = StringUtils.getJsonString(jsonObject, "redirect_url");

        userId = StringUtils.getJsonInt(jsonObject, "user_id");
        screen_name = StringUtils.getJsonString(jsonObject, "screen_name");
        title = StringUtils.getJsonString(jsonObject, "title");
        content = StringUtils.getJsonString(jsonObject, "content");
        created_time = StringUtils.getJsonString(jsonObject, "created_time");
        updated_date = StringUtils.getJsonString(jsonObject, "updated_date");
        sort = StringUtils.getJsonInt(jsonObject, "sort");
        sortPosition = StringUtils.getJsonInt(jsonObject, "position");

        praise_num = StringUtils.getJsonInt(jsonObject, "praise_num");
        comment_num = StringUtils.getJsonInt(jsonObject, "comment_num");
        isPraise = StringUtils.getJsonInt(jsonObject, "is_praise");

        iconUrl = StringUtils.getJsonString(jsonObject, "icon");
        typeIcon = StringUtils.getJsonString(jsonObject, "type_icon");
        avatUrl = StringUtils.getJsonString(jsonObject, "id_url_avat");
//        if(type == IHomeDynamicType.CIRCLE_RECOMMEND){  //首页圈子推荐的格式跟其它不一致
//            JSONObject publisherObj = StringUtils.getJsonObejct(jsonObject,"publisher");
//            publisher = StringUtils.getJsonString(publisherObj,"screen_name");
//        }else {
        publisher = StringUtils.getJsonString(jsonObject, "publisher");
//        }
        circleName = StringUtils.getJsonString(jsonObject, "circle_name");
        forumId = StringUtils.getJsonInt(jsonObject, "forum_id");
        idUrl = StringUtils.getJsonString(jsonObject, "id_url");
        isMoreComment = StringUtils.getJsonInt(jsonObject, "is_more");
        shareWords = StringUtils.getJsonString(jsonObject, "words");
        originalId = StringUtils.getJsonInt(jsonObject, "original_id");
        allow_operate = StringUtils.getJsonBoolean(jsonObject, "allow_operate");
        isExpand = StringUtils.getJsonInt(jsonObject, "is_expand");
        recommType = StringUtils.getJsonString(jsonObject, "recomm_type");
        tipUrl = StringUtils.getJsonString(jsonObject, "tip_url");
        tipCategory = StringUtils.getJsonString(jsonObject, "tip_category");
        tipCategoryId = StringUtils.getJsonInt(jsonObject, "tip_category_id");
        tipId = StringUtils.getJsonInt(jsonObject, "tip_id");
        topicId = StringUtils.getJsonInt(jsonObject, "topic_id");
        isvip = StringUtils.getJsonInt(jsonObject, "isvip");
        user_type = StringUtils.getJsonInt(jsonObject, "user_type");
        isRecommend = StringUtils.getJsonInt(jsonObject, "is_recommend");
        related = StringUtils.getJsonString(jsonObject, "related");
        dynamicNum = StringUtils.getJsonInt(jsonObject, "dynamic_num");
        fans = StringUtils.getJsonInt(jsonObject, "funs");
        goHomepage = StringUtils.getJsonInt(jsonObject, "go_homepage");
        isFollow = StringUtils.getJsonInt(jsonObject, "is_follow");
        info = StringUtils.getJsonString(jsonObject, "info");
        isPublic = StringUtils.getJsonInt(jsonObject, "is_public");

        toolUrl = StringUtils.getJsonString(jsonObject, "tool_url");
        video_time = StringUtils.getJsonString(jsonObject, "video_time");


        JSONObject avatar = StringUtils.getJsonObejct(jsonObject, "avatar");
        if (null != avatar) {
            avatarModel = new TopicAvatarModel(avatar);
        }


        JSONArray imagesArray = StringUtils.getJsonArray(jsonObject, "images");
        if (null != imagesArray && imagesArray.length() > 0) {
            imagesList = new ArrayList<String>();
            for (int i = 0; i < imagesArray.length(); i++) {
                try {
                    imagesList.add(imagesArray.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


    }

}
