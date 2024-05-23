package com.pass.data

import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.pass.data.repository.ProfileRepositoryImpl
import com.pass.data.util.FirebaseAuthUtil
import com.pass.data.util.FirebaseDatabaseUtil
import com.pass.data.util.FirebaseStorageUtil
import com.pass.domain.model.Profile
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProfileRepositoryUserProfileTest {

    // firebase 모킹
    private val auth = mockk<FirebaseAuth>()
    private val fireStore = mockk<FirebaseFirestore>()
    private val storage = mockk<FirebaseStorage>()

    // util 선언
    private val firebaseAuthUtil = FirebaseAuthUtil(auth)
    private val firebaseDatabaseUtil = FirebaseDatabaseUtil(auth, fireStore)
    private val firebaseStorageUtil = FirebaseStorageUtil(storage)

    // fireStore 모킹
    private val mockFireStoreTask = mockk<Task<DocumentSnapshot>>()
    private val mockFireStoreException = RuntimeException("Error")
    private val mockDocumentSnapshot = mockk<DocumentSnapshot>()
    private val mockFireStoreTransactionTask = mockk<Task<Any>>()

    // firebase storage 모킹
    private val mockUploadTask = mockk<UploadTask>()
    private val mockUpStorageTask = mockk<StorageTask<UploadTask.TaskSnapshot>>()
    private val mockUploadTaskSnapshot = mockk<UploadTask.TaskSnapshot>()
    private val mockDownUriTask = mockk<Task<Uri>>()
    private val mockDownloadUri = mockk<Uri>()

    private val testProfile = Profile("test name", "", listOf())

    // repository 초기화
    private val profileRepositoryImpl = ProfileRepositoryImpl(auth, firebaseAuthUtil, firebaseDatabaseUtil, firebaseStorageUtil)

    @Test
    fun testSuccessGetUserProfile() = runBlocking {
        // fireStore getUserProfile 성공 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockDocumentSnapshot.toObject(Profile::class.java) } returns testProfile
        every { mockFireStoreTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }

        // fireStore getUserProfile 성공 시나리오 테스트
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailGetUserProfile() = runBlocking {
        // fireStore getUserProfile 실패 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTask.isSuccessful } returns false

        // listener 실패 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTask
        }

        // firebase collection  메서드를 호출에 대한 실패 모킹 적용
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUid() = runBlocking {
        every { auth.currentUser?.uid } returns null

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailGetUserProfileWithNullUser() = runBlocking {
        // fireStore getUserProfile 성공 + user null 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockDocumentSnapshot.toObject(Profile::class.java) } returns null
        every { mockFireStoreTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockFireStoreTask
        }
        every { mockFireStoreTask.addOnFailureListener(any()) } answers {
            mockFireStoreTask
        }

        // firebase collection  메서드를 호출에 대한 실패 모킹 적용
        every { fireStore.collection(any()).document(any()).get() } returns mockFireStoreTask

        // fireStore getUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.getUserProfile().first()
        assertTrue(result.isFailure)
        assertEquals("프로필을 조회할 수 없습니다.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testSuccessUpdateUserProfileName(): Unit = runBlocking {
        // fireStore updateUserProfile 성공 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTransactionTask.isSuccessful } returns true

        // listener 성공 가정
        every { mockFireStoreTransactionTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Unit>>().onSuccess(null)
            mockFireStoreTransactionTask
        }
        every { mockFireStoreTransactionTask.addOnFailureListener(any()) } answers {
            mockFireStoreTransactionTask
        }

        // firestore runtransaction 호출에 대한 메소드 모킹 적용
        every { fireStore.runTransaction<Any>(any()) } answers {
            mockFireStoreTransactionTask
        }

        // fireStore updateUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.updateUserProfile("test", "name").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateUserProfile() = runBlocking {
        // fireStore updateUserProfile 실패 가정
        every { auth.currentUser?.uid } returns "test123"
        every { mockFireStoreTransactionTask.isSuccessful } returns false

        // listener 실패 가정
        every { mockFireStoreTransactionTask.addOnSuccessListener(any()) } answers {
            mockFireStoreTransactionTask
        }
        every { mockFireStoreTransactionTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockFireStoreTransactionTask
        }

        // firestore runtransaction 호출에 대한 메소드 모킹 적용
        every { fireStore.runTransaction<Any>(any()) } answers {
            mockFireStoreTransactionTask
        }

        // fireStore updateUserProfile 실패 시나리오 테스트
        val result = profileRepositoryImpl.updateUserProfile("test", "name").first()
        assertTrue(result.isFailure)
        assertEquals(mockFireStoreException.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun testFailUpdateUserProfileNameWithNullUid() = runBlocking {
        every { auth.currentUser?.uid } returns null

        val result = profileRepositoryImpl.updateUserProfile("test", "name").first()
        assertTrue(result.isFailure)
        assertEquals("오류가 발생하였습니다. 다시 로그인을 진행해주세요.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testSuccessUpdateUserProfilePicture() = runBlocking {
        every { mockUploadTask.isSuccessful } returns true
        every { mockDownUriTask.isSuccessful } returns true
        every { mockUploadTask.result } returns mockUploadTaskSnapshot
        every { mockUpStorageTask.result } returns mockUploadTaskSnapshot

        every { mockUploadTaskSnapshot.metadata?.reference?.downloadUrl } returns mockDownUriTask
        every { mockDownloadUri.toString() } returns "test1234"

        every { storage.reference.child(any()).putFile(any()) } returns mockUploadTask

        // listener 성공 가정
        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskSnapshot)
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            mockUpStorageTask
        }
        every { mockUpStorageTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<UploadTask.TaskSnapshot>>().onSuccess(mockUploadTaskSnapshot)
            mockUpStorageTask
        }
        every { mockUpStorageTask.addOnFailureListener(any()) } answers {
            mockUpStorageTask
        }
        every { mockDownUriTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Uri>>().onSuccess(mockDownloadUri)
            mockDownUriTask
        }
        every { mockDownUriTask.addOnFailureListener(any()) } answers {
            mockDownUriTask
        }

        // 이름 수정 기능 테스트 (중복)
        testSuccessUpdateUserProfileName()

        // updateUserProfile 성공 시나리오 테스트
        val result = profileRepositoryImpl.updateUserProfilePicture("test1234").first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testFailUpdateUserProfilePicture() = runBlocking {
        every { mockUploadTask.isSuccessful } returns false

        every { storage.reference.child(any()).putFile(any()) } returns mockUploadTask

        // listener 실패 가정 - 첫 번째 리스너 (reference 에 사진 등록 기능)
        every { mockUploadTask.addOnSuccessListener(any()) } answers {
            mockUploadTask
        }
        every { mockUploadTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(mockFireStoreException)
            mockUpStorageTask
        }

        testSuccessUpdateUserProfileName()

        val result = profileRepositoryImpl.updateUserProfilePicture("test1234").first()
        assertTrue(result.isFailure)
    }
}