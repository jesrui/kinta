package com.dailymotion.kinta.integration.git

import com.dailymotion.kinta.Project
import com.dailymotion.kinta.integration.commandline.CommandLine
import org.eclipse.jgit.api.Git

object GitIntegration {
    fun push(remote: String? = null,
             src: String? = null,
             dst: String? = null
    ) {
        val remote_ = remote ?: "origin"
        val src_ = src ?: currentBranch()
        val dst_ = dst ?: src_

        CommandLine.execute("git push $remote_ $src_:$dst_")
    }

    fun currentBranch(): String {
        return Project.repository.branch!!
    }

    fun checkout(branch: String = "master") {
        val git = Git(Project.repository)
        git.checkout()
                .setName(branch)
                .call()
    }

    fun pull() {
        CommandLine.execute("git pull")
    }

    fun createBranch(branchName: String) {
        val git = Git(Project.repository)
        git.checkout()
                .setCreateBranch(true)
                .setName(branchName)
                .call()
    }

    fun checkCleanliness() {
        val git = Git(Project.repository)

        val status = git.status().call()
        check(status.isClean) {
            "Your working copy needs to be clean. Either commit or stash your devs"
        }
    }

    fun fetch(remote: String? = null, prune: Boolean = false) {
        val remote_ = remote ?: "origin"

        val prune_ = if (prune) "-p" else ""
        CommandLine.execute("git fetch $remote_ $prune_")
    }

    fun tag(tagName: String) {
        val git = Git(Project.repository)
        val tag = git.tag()
        tag.name = tagName
        tag.call()
    }

    fun add(filePath: String){
        val git = Git(Project.repository)
        val addCommand = git.add()
        addCommand.addFilepattern(filePath)
        addCommand.call()
    }

    fun commit(message: String) {
        val git = Git(Project.repository)
        val commit = git.commit()
        commit.message = message
        commit.setAll(true)
        commit.call()
    }

    fun getBranches(): List<String> {
        val git = Git(Project.repository)

        return git.branchList().call()
                .map { it.name.substring("refs/heads/".length) }
    }

    fun deleteBranches(branches: List<String>, force: Boolean = false) {
        val git = Git(Project.repository)
        val command = git.branchDelete()
        command.setBranchNames(*branches.toTypedArray())
        command.setForce(force)
        command.call()
    }

    fun getUserEmail() = Git(Project.repository).repository.config.getString("user", null, "email")
}