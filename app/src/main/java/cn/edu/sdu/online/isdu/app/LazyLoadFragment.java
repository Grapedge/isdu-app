package cn.edu.sdu.online.isdu.app;

import android.support.v4.app.Fragment;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/19
 *
 * 实现懒加载功能的Fragment
 * 适用于网络等操作的限制
 * 继承{@link android.support.v4.app.Fragment}
 * 一般需要重写{@link #loadData()}和{@link #publishData()}方法
 * 在加载完毕后需要手动调用{@link #publishData()}进行数据展示
 * 在特殊情况下（流量消耗比较大）需要重写{@link #isLoadComplete()}方法
 ****************************************************
 */
public abstract class LazyLoadFragment extends Fragment {

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        if (isVisibleToUser) {
            if (isLoadComplete())
                publishData();
            else
                loadData();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    /**
     * 数据是否加载完成
     * 在{@link #setUserVisibleHint(boolean)}中，若返回true则
     * 调用{@link #publishData()}方法，不再调用{@link #loadData()}方法加载数据
     *
     * @return 是否完成数据加载
     */
    protected boolean isLoadComplete() {
        return false;
    }

    /**
     * 加载数据
     * 可以进行另开线程的耗时操作
     * 默认在UIThread上运行
     *
     * 在数据加载完成之后需要手动调用{@link #publishData()}方法
     */
    public void loadData() {}

    /**
     * 刷新已经加载完成的数据
     * 需要重写{@link #isLoadComplete()}方法来决定是否自动显示
     * 已加载的数据
     */
    protected void publishData() {}

}
