package org.example.domain.wiseSaying.simpledb;

import org.example.domain.wiseSaying.entity.WiseSaying;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecuteResult {
    //    ResultSet rs;
    private List<Map<String, Object>> selectRows;
    private int totalCount = 0;

//    public ExecuteResult(){}
    public ExecuteResult(List<Map<String, Object>> selectRows) {
        this.selectRows = selectRows;

        Map<String, Object> firstItem = selectRows.getFirst();
        Number count = (Number) firstItem.getOrDefault("total_count", 0L);
        this.totalCount = count.intValue();
    }

    public List<Map<String, Object>> getSelectRows() {
        return selectRows;
    }

    public void setSelectRows(List<Map<String, Object>> selectRows) {
        this.selectRows = selectRows;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * Article만 동작해~
     *
     * @param <T>
     * @return {list<T>}
     */
    public <T> List<T> listToGeneral(Class<T> who) {
        List<T> resultList = new ArrayList<>();

        for (Map<String, Object> rowMap : selectRows) {
            try {
                T newItem = who.getDeclaredConstructor().newInstance();
                /*
                if (newItem instanceof Article article) {
                    article.setData(rowMap);
                    resultList.add(who.cast(article));
                }*/
                if (newItem instanceof WiseSaying wiseSaying) {
                    wiseSaying.setData(rowMap);
                    resultList.add(who.cast(wiseSaying));
                }
            } catch (Exception e) {
                System.out.println(e);
                continue;
            }
        }
        return resultList;
    }

}
