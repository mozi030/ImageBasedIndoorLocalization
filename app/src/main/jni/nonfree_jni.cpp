#include <opencv2/core/core.hpp>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>
#include <opencv2/legacy/legacy.hpp>
#include <string>
#include <vector>
#include <map>
#include <queue>
#include <opencv2/highgui/highgui.hpp>
#include <jni.h>

#include <unistd.h>
#include <ctime>
#include <cstdlib>
#include <algorithm>
#include <cstring>

#include <android/log.h>

#include <pthread.h>
#include <iostream>
#include <fstream>
#include <dirent.h>

//#include "MyMatch.cpp"
//#include "triangle_calculator/triangleCalc.cpp"
#include "triangleCalc.cpp"

#include "vlfeat/kdtree.h"
#include "vlfeat/kmeans.h"

using namespace std;
using namespace cv;

clock_t start_time;
Mat whole_image, *partition_image;
vector<KeyPoint> detect_keypoints;
vector<KeyPoint>*k;
pthread_t* tids;

int finished_thread_num = 0;
int entered_i = 0;
int folder_num = 84;
int feature_num = 500;
int dimension = 64;

int thread_num = 1;
int* answer_num;
int* answer_nums[100];

float match_threshold = 1000;

VlKDForest *kdtree;
float *kdTree_data;

double total_match_time;

bool isTreeBuilt = false;

bool comp(KeyPoint kp1, KeyPoint kp2) {
	return kp1.response > kp2.response;
}

//vlfeat code begin
void build_kdTree_with_vlfeat() {
	kdTree_data = new float[folder_num * dimension * feature_num];
	int index = 0;

	FILE *file = NULL;
	for (int i = 0; i < folder_num; i++) {
		char *filename = new char[200];
		sprintf(filename, "/sdcard/data/manyImages/data_v2/Features/%d.SURF.features", i + 1);
		if ((file = fopen(filename, "r"))) {
			while (fscanf(file, "%f,", kdTree_data + index) != EOF) {
				index++;
			}
		}
		delete []filename;
	}

	kdtree = vl_kdforest_new(VL_TYPE_FLOAT, 64, 1, VlDistanceL2);
	vl_kdforest_set_max_num_comparisons(kdtree, 400);
	vl_kdforest_set_thresholding_method(kdtree, VL_KDTREE_MEDIAN);

	vl_kdforest_build(kdtree, feature_num * folder_num, kdTree_data);
}

int search_kdTree_with_vlfeat(float* feature) {
	VlKDForestNeighbor *neighbor = new VlKDForestNeighbor;
	vl_kdforest_query(kdtree, neighbor, 1, feature);
	int answer = neighbor[0].index / feature_num + 1;
	delete neighbor;
	return answer;
}

vector<int> search_kdTree_with_vlfeat_modify(float* feature) {
	vector<int> result;
	int k_neighbor = 5;

	VlKDForestNeighbor *neighbor = new VlKDForestNeighbor[k_neighbor];
	vl_kdforest_query(kdtree, neighbor, k_neighbor, feature);

	double max_distance = 0.05;

	for (int i = 0; i < k_neighbor - 1; i++) {

		int answer = neighbor[i].index / feature_num + 1;
		if (neighbor[i].distance < max_distance
				&& neighbor[i].distance < 0.8 * neighbor[i + 1].distance) {
			result.push_back(answer);
		}
//		LOGD("neighbor %d distance = %f", i, neighbor[i].distance);
	}

	delete[] neighbor;
//	LOGD("result size is %d", result.size());
	return result;
}

void del_in_vlfeat() {
	delete[] kdTree_data;
}
//vlfeat code end

void* thread_fun(void* args) {
	int i = *((int*) args);
	entered_i++;
	//�̳߳���
	//����detect
//	vector<KeyPoint> keypoints;
//	SurfFeatureDetector detector;
//	detector.detect(partition_image[i], keypoints);

	//strongest feature
//	int actualStrongestFeature = 0;
//	if (strongestFeatures <= k[i].size()) {
//		actualStrongestFeature = strongestFeatures;
//	} else {
//		actualStrongestFeature = k[i].size();
//	}
//	std::sort(k[i].begin(), k[i].begin() + actualStrongestFeature, comp);
//	std::vector<KeyPoint> strongestKeypoints(k[i].begin(),
//			k[i].begin() + actualStrongestFeature);

	//����extract
	SurfDescriptorExtractor extractor;
	Mat descriptor;
	//extractor.compute(partition_image[i], strongestKeypoints, descriptor);
	extractor.compute(whole_image, k[i], descriptor);
	//extractor.compute(whole_image, k[i], descriptor);

	//����match
	vector<int> answers;
//	vector<float> answer_distances;
	for (int j = 0; j < descriptor.rows; j++) {
		float*search_data = descriptor.ptr<float>(j);
		//opencv match
//		MyMatch* mymatch = new MyMatch();
//		vector<nearest_distance_node*> answer = mymatch->searchKDTree(
//				search_data);
//		for (int k = 0; k < answer.size(); k++) {
//			if (answer[k]->this_distance < match_threshold) {
//				answers.push_back(answer[k]->node_num);
////				answer_distances.push_back(answer[k]->this_distance);
//			}
//		}
//		delete mymatch;

		//vlfeat match
//		float*temp = new float[64];
//		for (int k = 0; k < 64; k++) {
//			temp[k] = search_data[k];
//		}
		clock_t t = clock();
		vector<int> result = search_kdTree_with_vlfeat_modify(search_data);
        //LOGD("result size is %d", result.size());
        for (int k = 0; k < result.size(); k++) {
            answers.push_back(result[k]);
        }
		total_match_time += double(clock() - t) / double(CLOCKS_PER_SEC);
//		delete[]temp;
	}

	while (i > finished_thread_num) {
		sleep(0.005);
	}
	for (int j = 0; j < answers.size(); j++) {
		answer_num[answers[j] - 1]++;
	}
	finished_thread_num++;
}

void getFiles(const char * files_folder, map<string, int>& files) {
	for (int i = 1; i <= folder_num; i++) {
		string a = "";
		if (i < 10) {
			a += char('0' + i);
		} else {
			a += char('0' + i / 10);
			a += char('0' + i % 10);
		}
		a += "/";

		DIR *pDir = NULL;
		struct dirent *dmsg;
		char szFolderName[128];
		strcpy(szFolderName, files_folder);
		strcat(szFolderName, a.c_str());
		if ((pDir = opendir(szFolderName)) != NULL) {
			while ((dmsg = readdir(pDir)) != NULL) {
				if (strcmp(dmsg->d_name, ".") != 0
						&& strcmp(dmsg->d_name, "..") != 0) {
					string b = "";
					b += szFolderName;
					b += dmsg->d_name;
					files[b] = i;
				}
			}
		}
	}
}

void init() {
	start_time = clock();

	//�̱߳�־
	tids = new pthread_t[thread_num];

	//��ͼ
	partition_image = new Mat[thread_num];
	//��������
	k = new vector<KeyPoint> [thread_num];

	//ƥ������
	answer_num = new int[folder_num];
	memset(answer_num, 0, folder_num * sizeof(int));

}

void detect() {
	SurfFeatureDetector detector;
	detector.hessianThreshold = 1000;
	detector.nOctaveLayers = 3;
	detector.nOctaves = 4;
	//detector.upright = true;
	detector.detect(whole_image, detect_keypoints);

	//strongest features
	int actualStrongestFeature2 = 0, strongestFeatures2 = 500;
	if (strongestFeatures2 <= detect_keypoints.size()) {
		actualStrongestFeature2 = strongestFeatures2;
	} else {
		actualStrongestFeature2 = detect_keypoints.size();
	}
	std::sort(detect_keypoints.begin(), detect_keypoints.begin() + actualStrongestFeature2,
			comp);
	std::vector<KeyPoint> strongestKeypoints2(detect_keypoints.begin(),
			detect_keypoints.begin() + actualStrongestFeature2);
	detect_keypoints = strongestKeypoints2;

	//���detect���
//	ofstream fout1("/sdcard/data/manyImages/data_v2/my_test/detect_result2.txt");
//	for (int i = 0;i < keypoints.size();i++) {
//		fout1 << "(" << keypoints[i].pt.x << "," << keypoints[i].pt.y << ")" << endl;
//	}
//	fout1.close();

	//��detect����ϱ��������
//	string a = "";
//	if (store_num < 10) {
//		a += ('0' + store_num);
//	} else {
//		a += ('0' + store_num / 10);
//		a += ('0' + store_num % 10);
//	}
//	int fir = file_name.find_last_of("/");
//	int last = file_name.find_last_of(".");
//	file_name = file_name.substr(fir + 1, last - fir) + ".png";
//	Mat out_image;
//	string OutFile = "/sdcard/data/manyImages/data_v2/select_wrong_images/detect_result/" + a + ":" + file_name;
//	drawKeypoints(whole_image, keypoints, out_image, Scalar::all(-1),
//			DrawMatchesFlags::DEFAULT);
//	imwrite(OutFile, out_image);
}

void partition() {
	int row = detect_keypoints.size();

	int quotient = row / thread_num;
	int remainder = row % thread_num;
	int pointer = 0, i = 0;
	for (; i < remainder; i++) {
		k[i].assign(detect_keypoints.begin() + pointer,
				detect_keypoints.begin() + pointer + quotient + 1);
		pthread_create(&tids[i], NULL, thread_fun, (void*) &i);
		pointer += quotient + 1;
		while (entered_i != i + 1) {
			sleep(0.01);
		}
	}
	for (; i < thread_num; i++) {
		k[i].assign(detect_keypoints.begin() + pointer,
				detect_keypoints.begin() + pointer + quotient);
		pthread_create(&tids[i], NULL, thread_fun, (void*) &i);
		pointer += quotient;
		while (entered_i != i + 1) {
			sleep(0.01);
		}
	}
}

float* evaluate() {
	//ȡanswer_num�е�ǰ��
	int biggest1 = -1, want1 = -1, biggest2 = -1, want2 = -1, biggest3 = -1,
			want3 = -1;
	for (int i = 0; i < folder_num; i++) {
		if (answer_num[i] > biggest1) {
			biggest3 = biggest2;
			biggest2 = biggest1;
			biggest1 = answer_num[i];
			want3 = want2;
			want2 = want1;
			want1 = i;
		} else if (answer_num[i] > biggest2) {
			biggest3 = biggest2;
			biggest2 = answer_num[i];
			want3 = want2;
			want2 = i;
		} else if (answer_num[i] > biggest3) {
			biggest3 = answer_num[i];
			want3 = i;
		}
	}

	float* result = new float[4];
	result[0] = want1 + 1;
	result[1] = want2 + 1;
	result[2] = want3 + 1;
	result[3] = double(clock() - start_time) / double(CLOCKS_PER_SEC);
	//del_row.push(result);

	return result;
}

void del() {
	//delete
	delete[] tids;
	delete[] partition_image;
	delete[] k;
	finished_thread_num = 0;
	entered_i = 0;
}

float* run_demo() {
	init();

	detect();

	partition();

	while (finished_thread_num < thread_num) {
		sleep(0.01);
	}

	del();

	return evaluate();
}

// JNI interface functions, be careful about the naming.
extern "C" {
JNIEXPORT jfloatArray JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_runDemo(
		JNIEnv* env, jclass obj, jint ChooseFirstNNum);
JNIEXPORT jboolean JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_buildtree(
		JNIEnv* env, jclass obj);
JNIEXPORT jint JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_deletePointer(
		JNIEnv* env, jclass obj);
}
;

JNIEXPORT jint JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_deletePointer(
		JNIEnv* env, jclass obj) {
	//MyMatch::del_all();
	del_in_vlfeat();
	isTreeBuilt = false;
	return 0;
}

JNIEXPORT jboolean JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_buildtree(
		JNIEnv* env, jclass obj) {
	if (isTreeBuilt == true) {
		return true;
	} else {
		build_kdTree_with_vlfeat();
		isTreeBuilt = true;
		return jboolean(true);
	}
}

JNIEXPORT jfloatArray JNICALL Java_com_example_moziliang_indoorlocalizationrelease_NonfreeJNILib_runDemo(
		JNIEnv* env, jclass obj, jint ChooseFirstNNum) {
	const char* photo_path = "/sdcard/data/manyImages/my_photo/%d.jpeg";

    int choose_first_n = ChooseFirstNNum;
	__android_log_print(ANDROID_LOG_INFO, "javaCheck","runDemo choose_first_n: %d",choose_first_n);

	float*answer = new float[choose_first_n * 3];
	//del_row.push(answer);

    char current_path[50];
    for (int i = 0; i < choose_first_n; i++) {
        sprintf(current_path, photo_path, i + 1);

        whole_image = imread(current_path, CV_LOAD_IMAGE_GRAYSCALE);
        float* result = run_demo();
        answer[i * 3 + 0] = result[0];
        answer[i * 3 + 1] = result[1];
        answer[i * 3 + 2] = result[2];
	    __android_log_print(ANDROID_LOG_INFO, "javaCheck","runDemo result2: %d %d %d", (int)result[0], (int)result[1], (int)result[2]);

        answer_nums[i] = answer_num;
    }

	jfloatArray a = env->NewFloatArray(choose_first_n * 3);
	env->SetFloatArrayRegion(a, 0, choose_first_n * 3, answer);

	return a;
}