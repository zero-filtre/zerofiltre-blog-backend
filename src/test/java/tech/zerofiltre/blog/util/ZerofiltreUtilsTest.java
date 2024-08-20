package tech.zerofiltre.blog.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZerofiltreUtilsTest {

    @Test
    void defineStartDateAndEndDate() {
        //ARRANGE
        //ACT
        List<LocalDate> list = ZerofiltreUtils.defineStartDateAndEndDate();

        //ASSERT
        assertThat(list.get(0)).isBefore(list.get(1));
    }

    @Test
    void partitionList() {
        //ARRANGE
        List<Integer> listInteger = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ,15);
        int partitionSize = 4;

        //ACT
        Collection<List<Integer>> list = ZerofiltreUtils.partitionList(listInteger, partitionSize);

        //ASSERT
        assertThat(list.size()).isEqualTo(4);
        assertThat(list.contains(Arrays.asList(1, 2, 3, 4))).isTrue();
        assertThat(list.contains(Arrays.asList(5, 6, 7, 8))).isTrue();
        assertThat(list.contains(Arrays.asList(9, 10, 11, 12))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14 ,15))).isTrue();
        assertThat(list.contains(Arrays.asList(13, 14 ,15, 16))).isFalse();
    }
}