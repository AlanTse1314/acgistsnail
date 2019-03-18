// Snail��������SnailLauncher

#include "stdafx.h"
#include "SnailLauncher.h"
#include "jni.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

// ����JVM����JAVA����
bool startJVM();
typedef jint(JNICALL* JNICREATEPROC) (JavaVM**, void**, void*);

// SnailLauncher

BEGIN_MESSAGE_MAP(SnailLauncher, CWinApp)
END_MESSAGE_MAP()

// SnailLauncher ����

SnailLauncher::SnailLauncher()
{
	// Ӧ�ó��� ID��CompanyName.ProductName.SubProduct.VersionInformation
	SetAppID(_T("acgist.Snail.SnailLauncher.1.0.0"));
}

// Ψһ��һ�� SnailLauncher ����

SnailLauncher launcher;

// SnailLauncher ��ʼ��

BOOL SnailLauncher::InitInstance()
{
	CWinApp::InitInstance();

	EnableTaskbarInteraction(FALSE);

	startJVM();

	return TRUE;
}

int SnailLauncher::ExitInstance()
{
	return CWinApp::ExitInstance();
}

// ����JVM
bool startJVM(){
	// JVM��̬��
	TCHAR* jvmPath = _T(".\\java\\bin\\server\\jvm.dll");
 
	//JVM��������
	const int jvmOptionCount = 4;
	JavaVMOption jvmOptions[jvmOptionCount];
	jvmOptions[0].optionString = "-server";
	jvmOptions[1].optionString = "-Xmx128M";
	jvmOptions[2].optionString = "-Xmx128m";
	jvmOptions[3].optionString = "-Djava.class.path=.\\snail-1.0.0.jar";
 
	JavaVMInitArgs jvmInitArgs;
	jvmInitArgs.version = JNI_VERSION_10;
	jvmInitArgs.options = jvmOptions;
	jvmInitArgs.nOptions = jvmOptionCount;

	// �����޷�ʶ��jvm�����
	jvmInitArgs.ignoreUnrecognized = JNI_TRUE;
 
	// ���������࣬ע��ָ�������/�����������ã�.��
	const char startClass[] = "com/acgist/main/Application";

	// ��������������main����
	const char startMethod[] = "main";

	// �������
	// int nParamCount = 2;
	// const char* params[nParamCount] = {"a","b"};
 
	// ����JVM DLL��̬���ӿ�
	HINSTANCE jvmDLL = LoadLibrary(jvmPath);
	if(jvmDLL == NULL){
		DWORD x = GetLastError();
		//outLog("����JVM��̬�����",GetLastError());
		return false;
	}
 
	// ��ʼ��JVM
	JNICREATEPROC jvmProcAddress = (JNICREATEPROC) GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
	if(jvmDLL == NULL){
		FreeLibrary(jvmDLL);
		//outLog("��ʼ��JVM�����ַʧ��", GetLastError());
		return false;
	}

	// ����JVM
	JNIEnv* env;
	JavaVM* jvm;
	jint jvmProc = (jvmProcAddress) (&jvm, (void**) &env, &jvmInitArgs);
	if(jvmProc < 0 || jvm == NULL ||env == NULL){
		FreeLibrary(jvmDLL);
	//	outLog("����JVM����",GetLastError());
		return false;
	}
 
	// ����������
	jclass mainClass = env -> FindClass(startClass);
	if(env -> ExceptionCheck() == JNI_TRUE || mainClass == NULL){
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
	//	outLog("����������ʧ��",GetLastError());
		return false;
	}
 
	// ������������
	jmethodID methedID = env -> GetStaticMethodID(mainClass, startMethod, "([Ljava/lang/String;)V");
	if(env -> ExceptionCheck() == JNI_TRUE || methedID == NULL){
		env -> ExceptionDescribe();
		env -> ExceptionClear();
		FreeLibrary(jvmDLL);
	//	outLog("������������ʧ��",GetLastError());
		return false;
	}
	
	env -> CallStaticVoidMethod(mainClass, methedID, NULL);
 
	// �ͷ�JVM
	jvm -> DestroyJavaVM();

	return true;
}