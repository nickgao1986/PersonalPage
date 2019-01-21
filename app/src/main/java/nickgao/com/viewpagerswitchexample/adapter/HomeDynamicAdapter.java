package nickgao.com.viewpagerswitchexample.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nickgao.com.viewpagerswitchexample.R;
import nickgao.com.viewpagerswitchexample.Util.StringUtils;
import nickgao.com.viewpagerswitchexample.model.HomeDynamicModel;
import nickgao.com.viewpagerswitchexample.view.PraiseButton;


public class HomeDynamicAdapter extends BaseAdapter {
    private static final int LAYOUT_TYPE_DEFAULT = 0;  //默认  说说布局
    private static final int LAYOUT_TYPE_SHARE = 1;    //话题分享布局
    private static final int LAYOUT_TYPE_CIRCLE_RECOMMEND = 2;  //话题推荐布局

    private static final int TYPE_COUNT = LAYOUT_TYPE_CIRCLE_RECOMMEND + 1;//3种布局
    private Activity context;
    private List<HomeDynamicModel> models;

    //是否是个人主页
    private boolean isPersonal = false;


    public HomeDynamicAdapter(Activity context) {
        this.context = context;
        models = new ArrayList<>();
    }

    public HomeDynamicAdapter(Activity context, List<HomeDynamicModel> dataList) {
        this.context = context;
        models = dataList;
    }

    public void setList(List<HomeDynamicModel> dataList) {
        this.models = dataList;
        notifyDataSetChanged();
    }

    /**
     * 增加数据
     *
     * @param newModels
     */
    public void addDatas(List<HomeDynamicModel> newModels) {
        if (newModels != null)
            models.addAll(newModels);
        notifyDataSetChanged();
    }

    /**
     * 在第一位添加数据
     *
     * @param newModel
     */
    public void addDataInHead(HomeDynamicModel newModel) {
        if (newModel == null)
            return;
        models.add(0, newModel);
        notifyDataSetChanged();
    }

    /**
     * 获取所有数据
     *
     * @return
     */
    public List<HomeDynamicModel> getDatas() {
        return models;
    }

    /**
     * 获取指定位置数据
     *
     * @param position
     * @return
     */
    public HomeDynamicModel getData(int position) {
        try {
            return models.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取最后一条数据
     *
     * @return
     */
    public HomeDynamicModel getLastData() {
        if (models.size() > 0) {
            return models.get(models.size() - 1);
        }
        return null;
    }

    /**
     * 删除数据
     *
     * @param homeDynamicModel
     */
    public void deleteData(HomeDynamicModel homeDynamicModel) {
        try {
            Iterator<HomeDynamicModel> iterator = models.iterator();
            while (iterator.hasNext()) {
                HomeDynamicModel model = iterator.next();
                if (model.id > 0) {
                    if (model.id == homeDynamicModel.id) {
                        iterator.remove();
                        break;
                    }
                } else {
                    if (model.uuid.equals(homeDynamicModel.uuid)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHome(boolean isPersonal) {
        this.isPersonal = isPersonal;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }


    @Override
    public int getItemViewType(int position) {
        return LAYOUT_TYPE_DEFAULT;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }



    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        try {
            ViewHolder viewHolder;
            int layoutType = getItemViewType(position);
            if (view == null) {
                viewHolder = new ViewHolder();
                switch (layoutType) {
                    case LAYOUT_TYPE_DEFAULT:
                        view = this.context.getLayoutInflater().inflate(R.layout.layout_home_dynamic_item, viewGroup, false);
//                        viewHolder.vsImages = (ViewStub) view.findViewById(R.id.vsImages);
//                        viewHolder.vsImageGrid = (ViewStub) view.findViewById(R.id.vsImagesGrid);
                        viewHolder.tvContent = (TextView) view.findViewById(R.id.tvContent);
                        viewHolder.ivMoreItem = (ImageView) view.findViewById(R.id.ivItemMore);
                        viewHolder.tvTime = (TextView) view.findViewById(R.id.tvTime);
                        break;
//                    case LAYOUT_TYPE_SHARE:
//                        view = this.context.getLayoutInflater().inflate(R.layout
//                                .layout_home_dynamic_item_share, viewGroup, false);
//                        viewHolder.tvContent = (CustomUrlTextView) view.findViewById(R.id.tvContent);
//                        viewHolder.ivShareIcon = (LoaderImageView) view.findViewById(R.id.ivShareIcon);
//                        viewHolder.tvShareTitle = (TextView) view.findViewById(R.id.tvShareContent);
//                        viewHolder.llShareContent = (LinearLayout) view.findViewById(R.id.llShareContent);
//                        // viewHolder.tvWatchMore = (TextView) view.findViewById(R.id.tvWatchMore);
//                        viewHolder.tvSharePublisher = (TextView) view.findViewById(R.id.tvSharePublisher);
//                        // viewHolder.tv_video_time = (TextView) view.findViewById(R.id.tv_video_time);
//                        viewHolder.ivMoreItem = (ImageView) view.findViewById(R.id.ivItemMore);
//                        viewHolder.tvTime = (TextView) view.findViewById(R.id.tvTime);
//                        // viewHolder.tvTypeFrom = (TextView) view.findViewById(tvTypeFrom);
//
//                        break;
                }

                viewHolder.rlItemContainer = (RelativeLayout) view.findViewById(R.id.llItemContainer);
                viewHolder.ivAvatar = (ImageView) view.findViewById(R.id.ivAvatar);
                viewHolder.tvNickname = (TextView) view.findViewById(R.id.tvNickname);

                viewHolder.btn_praise = (PraiseButton) view.findViewById(R.id.btn_praise);

//                viewHolder.llZan = (LinearLayout) view.findViewById(R.id.llZan);
//                viewHolder.ivZan = (ImageView) view.findViewById(R.id.ivZanImage);
//                viewHolder.tvZan = (TextView) view.findViewById(R.id.tvZan);

                viewHolder.tvReply = (TextView) view.findViewById(R.id.tvReply);
                viewHolder.llZanReply = (LinearLayout) view.findViewById(R.id.llZanReply);
                viewHolder.zanDivider = (ImageView) view.findViewById(R.id.zanDivider);

                viewHolder.divider = view.findViewById(R.id.divider);
                viewHolder.tvReply.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.tata_icon_commentthick),
                        null, null, null);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            final HomeDynamicModel homeDynamicModel = models.get(position);

            if (!StringUtils.isNull(homeDynamicModel.created_time)) {
                viewHolder.tvTime.setText(homeDynamicModel.created_time);
                viewHolder.tvTime.setVisibility(View.VISIBLE);
            }
            if (homeDynamicModel.allow_operate) {
                viewHolder.llZanReply.setVisibility(View.VISIBLE);
                viewHolder.tvReply.setVisibility(View.VISIBLE);

                viewHolder.tvReply.setText(StringUtils.getString(homeDynamicModel.comment_num));

                if (isPublicNewsType(homeDynamicModel)) {
                    viewHolder.btn_praise.setVisibility(View.GONE);
                    viewHolder.zanDivider.setVisibility(View.GONE);
                } else {
                    viewHolder.btn_praise.setVisibility(View.VISIBLE);
                    viewHolder.zanDivider.setVisibility(View.VISIBLE);

                    if (homeDynamicModel.isPraise == 1) {
                        viewHolder.btn_praise.setPraiseState(true);
                        viewHolder.btn_praise.setPraiseCount(homeDynamicModel.praise_num);
                    } else {
                        viewHolder.btn_praise.setPraiseState(false);
                        viewHolder.btn_praise.setPraiseCount(homeDynamicModel.praise_num);
                    }
                }
            } else {
                viewHolder.llZanReply.setVisibility(View.GONE);
                // viewHolder.tvReply.setVisibility(View.GONE);
            }


            switch (layoutType) {
                case LAYOUT_TYPE_DEFAULT:
                    viewHolder.tvNickname.setText(homeDynamicModel.screen_name);
                    if (!StringUtils.isNull(homeDynamicModel.content)) {
                        viewHolder.tvContent.setVisibility(View.VISIBLE);
                        viewHolder.tvContent.setText(homeDynamicModel.content);
                    } else {
                        viewHolder.tvContent.setVisibility(View.GONE);
                    }

//                    fillImages(view, viewHolder, homeDynamicModel, position);
                    break;

            }
           // fillAvatarIcon(viewHolder, homeDynamicModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }


    //这种类型不显示点赞
    private boolean isPublicNewsType(final HomeDynamicModel homeDynamicModel) {
        return false;
    }




    class ViewHolder {
        RelativeLayout rlItemContainer;
        ImageView ivAvatar;
        TextView tvNickname;
        TextView tvContent;
        LinearLayout llDynamicContent;

        LinearLayout llZanReply;
        PraiseButton btn_praise;
        ImageView zanDivider;
        TextView tvReply;

        ViewStub vsImages;
        ViewStub vsImageGrid;
        ViewStub vsFriendInfo;
        TextView tvRecommendResoan;

        LinearLayout llShareContent;
        ImageView ivShareIcon;
        TextView tvShareTitle;
        TextView tvSharePublisher;
        TextView tvSharePublisher2;
        ImageView ivMoreItem;
        TextView tvTime;

        View divider;
    }
}
