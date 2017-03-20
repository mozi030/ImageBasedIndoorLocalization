#include <vector>
#include <stdio.h>
#include <assert.h>
#include <cmath>
#include <set>

typedef struct mall {
    int id;
    float pos_x;
    float pos_y;

    bool operator<(const mall &other) const {
        return id < other.id;
    }

    mall& operator=(const mall &other) {
        id = other.id;
        pos_x = other.pos_x;
        pos_y = other.pos_y;

        return *this;
    }
} mall_t;

std::vector<mall_t> load_mall(const char *filename){
    std::vector<mall_t> mall_list;

    FILE * file = fopen(filename, "r");
    if (!file) {
        printf("error reading mall data file '%s'\n", filename);
    }

    while(!feof(file)) {
        mall_t tmp_mall;
        fscanf(file, "%d %f %f ",&tmp_mall.id, &tmp_mall.pos_x, &tmp_mall.pos_y);
        mall_list.push_back(tmp_mall);
    }

    fclose(file);
    return mall_list;
}

typedef struct point {
    float pos_x;
    float pos_y;

    point(float _x = 0, float _y = 0) {
        pos_x = _x;
        pos_y = _y;
    }
} point_t;

typedef struct rfpoint {
    point_t pos;
    int *visiable_state;
} rfpoint_t;

typedef struct model {
    point_t from;
    point_t to;
} model_t;

std::vector<model_t> load_mapmodel(const char *filename){
    std::vector<model_t> model_list;

    FILE *file = fopen(filename, "r");
    if (!file) {
        printf("error reading model data file '%s'\n", filename);
    }

    while (!feof(file)) {
        model_t tmp_model;
        fscanf(file, "%f %f %f %f ", &tmp_model.from.pos_x, &tmp_model.from.pos_y, &tmp_model.to.pos_x,
               &tmp_model.to.pos_y);
        model_list.push_back(tmp_model);
    }

    fclose(file);
    return model_list;
}

std::vector<rfpoint_t> load_rfpoints(const char *filename){
    std::vector<rfpoint_t> rfpoint_list;

    FILE *file = fopen(filename, "r");
    if (!file) {
        printf("error reading rfpoint data file '%s'\n", filename);
    }

    while (!feof(file)) {
        rfpoint_t tmp_rfpoint;
        fscanf(file, "%f %f ", &tmp_rfpoint.pos.pos_x, &tmp_rfpoint.pos.pos_y);
        int mall_count;
        fscanf(file, "%d ", &mall_count);
        tmp_rfpoint.visiable_state = new int[mall_count];
        for (int i = 0; i < mall_count; i++) {
            fscanf(file, "%d ", tmp_rfpoint.visiable_state + i);
        }
        rfpoint_list.push_back(tmp_rfpoint);
    }

    return rfpoint_list;
}


#define MAX_INT 2147483647

#define PI 3.14159265f
#define DEG2RAD(alpha) (alpha * PI / 180.0f)
#define RAD2DEG(alpha) (180.0f * alpha / PI)
#define SIGN(num) (num > 0 ? 1 : -1)
#define GET_MIN(a, b) (a < b ? a : b)
#define GET_MAX(a, b) (a > b ? a : b)

#define LAMBDA 5
#define EPSILON 20
#define MAX_LENGTH 1000
#define MIN_LENGTH 20
#define RF_NUMBER 3
#define METER 20

float dot_product(const point_t *veca, const point_t *vecb) {
    return veca->pos_x * vecb->pos_x + veca->pos_y * vecb->pos_y;
}

float norm(const point_t *vec) {
    return sqrtf(vec->pos_x * vec->pos_x + vec->pos_y * vec->pos_y);
}

bool cross(const point_t *from, const point_t *to) {
    return from->pos_x * to->pos_y - from->pos_y * to->pos_x > 0;
}

float pdist(const float *veca, const float *vecb) {
    return sqrtf((veca[0] - vecb[0]) * (veca[0] - vecb[0]) + (veca[1] - vecb[1]) * (veca[1] - vecb[1]) +
                 (veca[2] - vecb[2]) * (veca[2] - vecb[2]));
}

float pdist(const point_t *veca, const point_t *vecb) {
    return sqrtf((veca->pos_x - vecb->pos_x) * (veca->pos_x - vecb->pos_x) +
                 (veca->pos_y - vecb->pos_y) * (veca->pos_y - vecb->pos_y));
}

float pdist(const mall_t veca, const mall_t vecb) {
    return sqrtf((veca.pos_x - vecb.pos_x) * (veca.pos_x - vecb.pos_x) +
                 (veca.pos_y - vecb.pos_y) * (veca.pos_y - vecb.pos_y));
}


int min(const float *data, const int size){
    if (size < 1) {
        return -1;
    }

    float minimal = data[0];
    int index = 0;

    for (int i = 1; i < size; i++) {
        if (data[i] < minimal) {
            minimal = data[i];
            index = i;
        }
    }

    return index;
}

int max(const float *data, const int size){
    assert(size != 0);

    float maximal = data[0];
    int index = 0;

    for (int i = 1; i < size; i++) {
        if (data[i] > maximal) {
            maximal = data[i];
            index = i;
        }
    }

    return index;
}

//point_t *node(const point_t *x1, const point_t *y1, const point_t *x2, const point_t *y2) {
//    point_t *result = new point_t();
//
//    if (x1->pos_x == y1->pos_x) {
//        result->pos_x = x1->pos_x;
//        float k2 = (y2->pos_y - x2->pos_y) / (y2->pos_x - x2->pos_x);
//        float b2 = x2->pos_y - k2 * x2->pos_x;
//        result->pos_y = k2 * result->pos_x + b2;
//    } else if (x2->pos_x == y2->pos_x) {
//        result->pos_x = x2->pos_x;
//        float k1 = (y1->pos_y - x1->pos_y) / (y1->pos_x - x1->pos_x);
//        float b1 = x1->pos_y - k1 * x1->pos_x;
//        result->pos_y = k1 * result->pos_x + b1;
//    } else {
//        float k1 = (y1->pos_y - x1->pos_y) / (y1->pos_x - x1->pos_x);
//        float b1 = x1->pos_y - k1 * x1->pos_x;
//        float k2 = (y2->pos_y - x2->pos_y) / (y2->pos_x - x2->pos_x);
//        float b2 = x2->pos_y - k2 * x2->pos_x;
//        if (k1 == k2) {
//            result->pos_x = NAN;
//            result->pos_y = NAN;
//        } else {
//            result->pos_x = (b2 - b1) / (k1 - k2);
//            result->pos_y = k1 * result->pos_x + b1;
//        }
//    }
//
//    return result;
//}

float calculate_vector_angle(const point_t *from, const point_t *to){
    float cosA = dot_product(from, to) / (norm(from) * norm(to));
    if (fabsf(cosA) > 1) {
        cosA = SIGN(cosA);
    }

    float cos_angle = RAD2DEG(acosf(cosA));
    if (!cross(from, to)) {
        cos_angle = 360.0f - cos_angle;
    }
    return cos_angle;
}

float calculate_sensor_score(const float gyro[], const float theta[], const float gyro_[], const float theta_[]) {
    return pdist(theta, theta_) + LAMBDA * pdist(gyro, gyro_);
}

bool line_intersection_side(point_t A,point_t B,point_t C,point_t D) {
    float fC = (C.pos_y - A.pos_y) * (A.pos_x - B.pos_x) - (C.pos_x - A.pos_x) * (A.pos_y - B.pos_y);
    float fD = (D.pos_y - A.pos_y) * (A.pos_x- B.pos_x) - (D.pos_x - A.pos_x) * (A.pos_y - B.pos_y);

    if (fC * fD > 0) {
        return false;
    }
    return true;
}

bool is_can_see(const point_t *user_pos, const point_t *store_pos, const std::vector<model_t> model) {
    float dist = pdist(user_pos, store_pos);
    if (dist > MAX_LENGTH || dist < MIN_LENGTH) {
        return false;
    }

    for (int i = 0; i < model.size(); i++) {
        if(line_intersection_side(*user_pos, *store_pos, model[i].from, model[i].to)
           &&line_intersection_side(model[i].from, model[i].to, *user_pos, *store_pos)) {
            return false;
        }
    }
    return true;
}

void sort_store(std::vector<mall_t> &store_pos, float *gyro) {
    assert(store_pos.size() == 3);
    if (store_pos[0].pos_x < store_pos[1].pos_x) {
        if (store_pos[1].pos_x < store_pos[2].pos_x) {
            return;
        } else if (store_pos[0].pos_x < store_pos[2].pos_x) {
            std::swap(store_pos[1], store_pos[2]);
            std::swap(gyro[1], gyro[2]);
        } else {
            float tmp = gyro[0];
            gyro[0] = gyro[2];
            gyro[2] = gyro[1];
            gyro[1] = tmp;

            mall_t m_tmp = store_pos[0];
            store_pos[0] = store_pos[2];
            store_pos[2] = store_pos[1];
            store_pos[1] = m_tmp;
        }
    } else {
        if (store_pos[0].pos_x < store_pos[2].pos_x) {
            std::swap(store_pos[0], store_pos[1]);
            std::swap(gyro[0], gyro[1]);
        } else if (store_pos[2].pos_x < store_pos[1].pos_x) {
            std::swap(store_pos[0], store_pos[2]);
            std::swap(gyro[0], gyro[2]);
        } else {
            float tmp = gyro[0];
            gyro[0] = gyro[1];
            gyro[1] = gyro[2];
            gyro[2] = tmp;

            mall_t m_tmp = store_pos[0];
            store_pos[0] = store_pos[1];
            store_pos[1] = store_pos[2];
            store_pos[2] = m_tmp;
        }
    }
}

bool get_possible_store(const float *compass, const std::vector<rfpoint_t> rfpoint,
                        const std::vector<mall_t> correct_store, const std::vector<mall_t> mall_list,
                        std::vector<mall_t> &store_list, std::vector<rfpoint_t> &rfpoint_list) {
    bool is_valid = true;
    point_t *north = new point_t(-1, 0);

    store_list.clear();
    rfpoint_list.clear();

    for (int i = 0; i < rfpoint.size(); i++) {
        bool is_visiable = true;
        for (int j = 0; j < correct_store.size(); j++) {
            point_t *user_dir = new point_t(correct_store[j].pos_x - rfpoint[i].pos.pos_x,
                                            correct_store[j].pos_y - rfpoint[i].pos.pos_y);
            float theta_ = calculate_vector_angle(user_dir, north);
            delete user_dir;
            if (fabsf(theta_ - compass[j]) > EPSILON) {
                is_visiable = false;
                break;
            }
        }
        if (is_visiable) {
            for (int j = 0; j < correct_store.size(); j++) {
                if (rfpoint[i].visiable_state[correct_store[j].id - 1] == 0) {
                    is_visiable = false;
                    break;
                }
            }
        }

        if (is_visiable) {
            rfpoint_list.push_back(rfpoint[i]);
        }
    }

    std::set<mall_t> store_list_set;
    for (int i = 0; i < rfpoint_list.size(); i++) {
        for (int j = 0; j < mall_list.size(); j++) {
            bool flag = true;
            for (int k = 0; k < correct_store.size(); k++) {
                if (mall_list[j].id == correct_store[k].id) {
                    flag = false;
                    break;
                }
            }
            if (flag && rfpoint_list[i].visiable_state[mall_list[j].id - 1] == 1) {
                store_list_set.insert(mall_list[j]);
            }
        }
    }
    std::copy(store_list_set.begin(), store_list_set.end(), std::back_inserter(store_list));
    if (store_list.size() == 0) {
        for (int j = 0; j < mall_list.size(); j++) {
            bool flag = true;
            for (int k = 0; k < correct_store.size(); k++) {
                if (mall_list[j].id == correct_store[k].id) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                store_list.push_back(mall_list[j]);
            }
        }
        is_valid = false;
    }

    delete north;
    return is_valid;
}

point_t ori_locator(const std::vector<mall_t> store_pos_c, const float *gyro_c) {
    assert(store_pos_c.size() == 3);
    float *gyro = new float[3];
    point_t user_pos;

    gyro[0] = gyro_c[0];
    gyro[1] = gyro_c[1];
    gyro[2] = gyro_c[2];

    std::vector<mall_t> store_pos(store_pos_c);

    sort_store(store_pos, gyro);

    float alpha = gyro[2] - gyro[1];
    float beta = gyro[1] - gyro[0];

    float a = pdist(store_pos[1], store_pos[2]);
    float b = pdist(store_pos[0], store_pos[1]);

    point_t *d_1 = new point_t(store_pos[0].pos_x, store_pos[0].pos_y);
    point_t *d_2 = new point_t(store_pos[1].pos_x, store_pos[1].pos_y);
    point_t *d_3 = new point_t(store_pos[2].pos_x, store_pos[2].pos_y);

    point_t *from = new point_t(d_2->pos_x - d_3->pos_x, d_2->pos_y - d_3->pos_y);
    point_t *to = new point_t(d_2->pos_x - d_1->pos_x, d_2->pos_y - d_1->pos_y);
    float theta = calculate_vector_angle(from, to);

    float sin_beta_plus_theta = sinf(DEG2RAD(beta) + DEG2RAD(theta));
    float cot_alpha = 1 / tanf(DEG2RAD(alpha));
    float cos_beta_plus_theta = cosf(DEG2RAD(beta) + DEG2RAD(theta));
    float sin_beta = sinf(DEG2RAD(beta));

    float x0_left_up = sin_beta_plus_theta * cot_alpha + cos_beta_plus_theta;
    float x0_right_up = a * sin_beta * cot_alpha + b * cos_beta_plus_theta;
    float x0_left_bottom = b * sin_beta_plus_theta - a * sin_beta;
    float x0_right_bottom = b * cos_beta_plus_theta + a * sin_beta * cot_alpha;

    float y0_left_up = x0_left_up;
    float y0_right_up = x0_left_bottom;
    float y0_left_bottom = x0_left_bottom;
    float y0_right_bottom = x0_right_bottom;

    float x0 = a * b * x0_left_up * x0_right_up / (x0_left_bottom * x0_left_bottom + x0_right_bottom * x0_right_bottom);
    float y0 = a * b * y0_left_up * y0_right_up / (y0_left_bottom * y0_left_bottom + y0_right_bottom * y0_right_bottom);

    user_pos.pos_x = x0 * (d_3->pos_x - d_2->pos_x) / a - y0 * (d_3->pos_y - d_2->pos_y) / a + d_2->pos_x;
    user_pos.pos_y = x0 * (d_3->pos_y - d_2->pos_y) / a + y0 * (d_3->pos_x - d_2->pos_x) / a + d_2->pos_y;

    delete from;
    delete to;
    delete d_1;
    delete d_2;
    delete d_3;
    delete[] gyro;

    return user_pos;
}

point_t locator(const std::vector<mall_t> &store_pos, const float *gyro, const float *theta,
                const std::vector<rfpoint_t> &rfpoints,
                const std::vector<model_t> &model, float **score){
    assert(store_pos.size() == 3);
    //assert (score != NULL);

    point_t *north = new point_t(-1, 0);

    //__android_log_print(ANDROID_LOG_INFO, "javaCheck","rfpoints.size():%d", rfpoints.size());
    for (int i = 0; i < rfpoints.size(); i++) {
        bool is_visiable = true;
        float *theta_ = new float[3];

        for (int j = 0; j < store_pos.size(); j++) {
            point_t *user_dir = new point_t(store_pos[j].pos_x - rfpoints[i].pos.pos_x,
                                            store_pos[j].pos_y - rfpoints[i].pos.pos_y);
            theta_[j] = calculate_vector_angle(user_dir ,north);
            delete user_dir;
            //__android_log_print(ANDROID_LOG_INFO, "javaCheck","theta_[j]=%f", theta_[j]);
            if (fabsf(theta_[j] - theta[j]) > EPSILON) {
                is_visiable = false;
            }
        }
        //__android_log_print(ANDROID_LOG_INFO, "javaCheck","%d ====== is_visiable:%d", i, is_visiable?1:0);

        if (is_visiable) {
            for (int j = 0; j < store_pos.size(); j++) {
                if (rfpoints[i].visiable_state[store_pos[j].id - 1] == 0) {
                    is_visiable = false;
                    break;
                }
            }
        }
        //__android_log_print(ANDROID_LOG_INFO, "javaCheck","%d ====== is_visiable:%d",i, is_visiable?1:0);

        if (is_visiable) {
            point_t *direc = new point_t[3];
            for (int j = 0; j < 3; j++) {
                direc[j].pos_x = store_pos[j].pos_x - rfpoints[i].pos.pos_x;
                direc[j].pos_y = store_pos[j].pos_y - rfpoints[i].pos.pos_y;
            }

            float alpha_ = calculate_vector_angle(direc, direc + 1);
            float beta_ = calculate_vector_angle(direc, direc + 2);

            //float *gyro_ = new float[3]{0, alpha_, beta_};
            float *gyro_ = new float[3];
            gyro_[0] = 0;
            gyro_[1] = alpha_;
            gyro_[2] = beta_;

            (*score)[i] = calculate_sensor_score(gyro, theta, gyro_, theta_);

            delete[] gyro_;
            delete[] direc;
        } else {
            (*score)[i] = MAX_INT;
        }

        delete[] theta_;
    }

    int index = min(*score, rfpoints.size());
    point_t user_pos;
    //__android_log_print(ANDROID_LOG_INFO, "javaCheck","index:%d", index);

    if (index != -1 && (*score)[index] != MAX_INT) {
        //__android_log_print(ANDROID_LOG_INFO, "javaCheck","(*score)[index]:%f", (*score)[index]);
        point_t cur_pos = rfpoints[index].pos;
        std::vector<point_t> rffpoints;
        for (int i = -RF_NUMBER; i <= RF_NUMBER; i++) {
            for (int j = -RF_NUMBER; j <= RF_NUMBER; j++) {
                point_t tmp_point(cur_pos.pos_x + i * METER / 2, cur_pos.pos_y + j * METER / 2);
                rffpoints.push_back(tmp_point);
            }
        }

        float *in_score = new float[rffpoints.size()];
        for (int i = 0; i < rffpoints.size(); i++) {
            bool is_visiable = true;
            float *theta_ = new float[3];

            for (int j = 0; j < store_pos.size(); j++) {
                point_t *tmp_point = new point_t(store_pos[j].pos_x, store_pos[j].pos_y);
                if (!is_can_see(&rffpoints[i], tmp_point, model)) {
                    is_visiable = false;
                }
                delete tmp_point;
                if (!is_visiable) {
                    break;
                }
            }

            if (is_visiable) {
                for (int j = 0; j < store_pos.size(); j++) {
                    point_t *user_dir = new point_t(store_pos[j].pos_x - rffpoints[i].pos_x,
                                                    store_pos[j].pos_y - rffpoints[i].pos_y);
                    theta_[j] = calculate_vector_angle(user_dir, north);
                    delete user_dir;
                    if (fabsf(theta_[j] - theta[j]) > EPSILON) {
                        is_visiable = false;
                    }
                }
            }

            if (is_visiable) {
                point_t *direc = new point_t[3];
                for (int j = 0; j < 3; j++) {
                    direc[j].pos_x = store_pos[j].pos_x - rffpoints[i].pos_x;
                    direc[j].pos_y = store_pos[j].pos_y - rffpoints[i].pos_y;
                }

                float alpha_ = calculate_vector_angle(direc, direc + 1);
                float beta_ = calculate_vector_angle(direc, direc + 2);

                //float *gyro_ = new float[3]{0, alpha_, beta_};
				float *gyro_ = new float[3];
				gyro_[0] = 0;
				gyro_[1] = alpha_;
				gyro_[2] = beta_;

                in_score[i] = calculate_sensor_score(gyro, theta, gyro_, theta_);

                delete[] gyro_;
                delete[] direc;
            } else {
                in_score[i] = MAX_INT;
            }

            delete[] theta_;
        }

        int in_index = min(in_score, rffpoints.size());
        user_pos.pos_x = rffpoints[in_index].pos_x;
        user_pos.pos_y = rffpoints[in_index].pos_y;
        delete[] in_score;
    } else {
        user_pos = ori_locator(store_pos, gyro);
        //__android_log_print(ANDROID_LOG_INFO, "javaCheck","trianglar location");
        (*score)[0] = MAX_INT;
    }

    delete north;

    return user_pos;
}

std::vector<mall_t> mall_list;
std::vector<model_t> model_list;
std::vector<rfpoint_t> rfpoint_list;

point_t map_constraint_main(const std::vector<mall_t> match_store, const int *match_result,
                            const std::vector<std::vector<float> > match_score,
                            const float *gyro, const float *theta) {
    assert(match_store.size() == 3);

    point_t user_pos;

    int match_count = 0;
    for (int i = 0; i < match_store.size(); i++) {
        if (match_result[i]) {
            match_count++;
        }
    }
    //__android_log_print(ANDROID_LOG_INFO, "javaCheck","match count:%d", match_count);
    if (match_count == 3) {
        float *sensor_score = new float[rfpoint_list.size()];
        user_pos = locator(match_store, gyro, theta, rfpoint_list, model_list, &sensor_score);
        delete [] sensor_score;
    } else if (match_count == 0) {
        user_pos.pos_x = NAN;
        user_pos.pos_y = NAN;
    } else {
        std::vector<mall_t> correct_store;
        float *compass_data = new float[match_count];
        for (int i = 0, j = 0; i < match_store.size(); i++) {
            if (match_result[i] == 1) {
                correct_store.push_back(match_store[i]);
                compass_data[j++] = theta[i];
            }
        }
        std::vector<mall_t> candidate_store;
        std::vector<rfpoint_t> candidate_rfpoint;

        bool is_valid = get_possible_store(compass_data, rfpoint_list, correct_store, mall_list, candidate_store,
                                           candidate_rfpoint);

        std::vector<std::vector<mall_t> > virtual_store_list;
        if (match_count == 2) {
            for (int i = 0; i < candidate_store.size(); i++) {
                std::vector<mall_t> tmp;
                for (int k = 0; k < 3; k++) {
                    if (match_result[k] == 1) {
                        tmp.push_back(match_store[k]);
                    } else {
                        tmp.push_back(candidate_store[i]);
                    }
                }
                virtual_store_list.push_back(tmp);
            }
        } else if (match_count == 1) {
            for (int i = 0; i < candidate_store.size(); i++) {
                for (int j = 0; j < candidate_store.size(); j++) {
                    if (candidate_store[i].id == candidate_store[j].id) {
                        continue;
                    }
                    std::vector<mall_t> tmp;
                    bool flag = false;
                    for (int k = 0; k < 3; k++) {
                        if (match_result[k] == 1) {
                            tmp.push_back(match_store[k]);
                        } else if (!flag) {
                            tmp.push_back(candidate_store[i]);
                            flag = true;
                        } else {
                            tmp.push_back(candidate_store[j]);
                        }
                    }
                    virtual_store_list.push_back(tmp);
                }
            }
        }

        float *totscore = new float[virtual_store_list.size()];
        point_t *points = new point_t[virtual_store_list.size()];

        for (int i = 0; i < virtual_store_list.size(); i++) {
            int size = candidate_rfpoint.size() > 0 ? candidate_rfpoint.size() : 1;
            float *sensor_score = new float[size];
            points[i] = locator(virtual_store_list[i], gyro, theta, candidate_rfpoint, model_list, &sensor_score);
            //assert(sensor_score != nullptr);
            int index = min(sensor_score, candidate_rfpoint.size());
            float mismatch_score = 1;
            if (sensor_score[index] != MAX_INT) {
                for (int k = 0; k < 3; k++) {
                    if (match_result[k] == 0) {
                        mismatch_score *= match_score[k][virtual_store_list[i][k].id - 1];
                    }
                }
                totscore[i] = mismatch_score / sensor_score[index];
            } else {
                totscore[i] = 0;
            }
            delete [] sensor_score;
        }

        int tot_index = max(totscore, virtual_store_list.size());
        user_pos = points[tot_index];

        delete[] points;
        delete[] totscore;
        delete[] compass_data;
    }

    return user_pos;
}

float* calculation(
		int store_num1, int store_num2, int store_num3,
		float gyro1, float gyro2, float gyro3,
		float compass1, float compass2, float compass3,
		int match1, int match2, int match3,
		int* answer_num1, int* answer_num2, int* answer_num3) {

	//__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation START2");

	mall_list = load_mall("/sdcard/data/manyImages/data_v2/mall.txt");
    model_list = load_mapmodel("/sdcard/data/manyImages/data_v2/map_model.txt");
    rfpoint_list = load_rfpoints("/sdcard/data/manyImages/data_v2/rfpoints_new.txt");

    int store_num = 84;

	//__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation START3");

    std::vector<mall_t> stores;
	float gyro[3], compass[3];
	int match[3];
	std::vector<std::vector<float> > score;

	mall_t mall1, mall2, mall3;
	mall1.id = store_num1;
	mall2.id = store_num2;
	mall3.id = store_num3;
	for (int i = 0;i < mall_list.size();i++) {
		if (mall_list[i].id == mall1.id) {
			mall1.pos_x = mall_list[i].pos_x;
			mall1.pos_y = mall_list[i].pos_y;
		}
		if (mall_list[i].id == mall2.id) {
			mall2.pos_x = mall_list[i].pos_x;
			mall2.pos_y = mall_list[i].pos_y;
		}
		if (mall_list[i].id == mall3.id) {
			mall3.pos_x = mall_list[i].pos_x;
			mall3.pos_y = mall_list[i].pos_y;
		}
	}
	stores.push_back(mall1);
	stores.push_back(mall2);
	stores.push_back(mall3);

	match[0] = match1;
	match[1] = match2;
	match[2] = match3;
	gyro[0] = gyro1;
	gyro[1] = gyro2;
	gyro[2] = gyro3;
	compass[0] = compass1;
	compass[1] = compass2;
	compass[2] = compass3;

	std::vector<float>temp1, temp2, temp3;
	for (int i = 0;i < store_num;i++) {
		temp1.push_back((float)(answer_num1[i]));
		temp2.push_back((float)(answer_num2[i]));
		temp3.push_back((float)(answer_num3[i]));
	}
	score.push_back(temp1);
	score.push_back(temp2);
	score.push_back(temp3);

//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation mall1:%d %f %f",mall1.id,mall1.pos_x,mall1.pos_y);
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation mall1:%d %f %f",mall2.id,mall2.pos_x,mall2.pos_y);
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation mall1:%d %f %f",mall3.id,mall3.pos_x,mall3.pos_y);
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation match:%d %d %d",match[0], match[1],match[2]);
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation gyro:%f %f %f",gyro[0], gyro[1],gyro[2]);
//	__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation compass:%f %f %f",compass[0], compass[1],compass[2]);
//	for (int i = 0;i < score.size();i++) {
//		std::vector<float>temp = score[i];
//		for (int j = 0;j < temp.size();j++) {
//			__android_log_print(ANDROID_LOG_INFO, "javaCheck", "JNI:calcLocation score%d %f",i, temp[j]);
//		}
//	}

	point_t result_pos = map_constraint_main(stores, match, score, gyro, compass);
	float*answer = new float[2];
	answer[0] = result_pos.pos_x;
	answer[1] = result_pos.pos_y;
	return answer;
}
