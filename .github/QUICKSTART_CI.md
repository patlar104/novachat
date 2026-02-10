# Quick Start Guide: CI/CD for NovaChat

This is a quick reference for using the CI/CD system. For detailed documentation, see [CI_CD_SETUP.md](CI_CD_SETUP.md).

## 🚀 Getting Started (5 Minutes)

### 1. Install Git Hooks (Local Development)

```bash
# Run this once after cloning the repository
./.github/hooks/setup-hooks.sh
```

This enables:

- ✅ Auto-format Kotlin files before commit
- ✅ Run tests before push
- ✅ Validate commit message format

### 2. Start Developing

Just work normally! The system handles everything:

```bash
# Make changes
git add .

# Commit (hooks will auto-format and validate)
git commit -m "feat(chat): add new feature"

# Push (hooks will run tests)
git push
```

### 3. CI Runs Automatically

When you push or create a PR:

- ✅ Code is built
- ✅ Tests are run
- ✅ Lint checks executed
- ✅ Security scanned
- ✅ Results shown in PR

---

## 📋 Commit Message Format

Use Conventional Commits:

```
<type>(<scope>): <description>

Example:
feat(chat): add message sending
fix(ui): resolve button alignment
docs(readme): update installation
test(viewmodel): add unit tests
```

**Types**: feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert

---

## 🏷️ Creating a Release

### Simple Method (Recommended)

```bash
# Create and push a tag
git tag v1.0.0
git push origin v1.0.0
```

The release workflow will:

1. Build signed APK and AAB
2. Create a GitHub Release (draft)
3. Upload artifacts
4. (Optional) Deploy to Play Store

### Manual Method

Go to Actions → Release Build → Run workflow

---

## ⚙️ Available Workflows

| Workflow         | Trigger        | Purpose                  |
| ---------------- | -------------- | ------------------------ |
| **Android CI**   | Push/PR        | Build, test, lint        |
| **Security**     | Push/PR/Weekly | Scan for vulnerabilities |
| **Code Quality** | Push/PR        | Style and quality checks |
| **Release**      | Tag `v*.*.*`   | Build and deploy         |

---

## 🛠️ Common Commands

### Bypass Hooks (Use Sparingly)

```bash
# Skip hooks for one commit
git commit --no-verify

# Skip hooks for one push
git push --no-verify
```

### View Workflow Runs

```bash
# Open in browser
open https://github.com/patlar104/novachat/actions

# Or click the "Actions" tab in GitHub
```

### Update Dependencies

Dependabot creates PRs automatically. Just:

1. Review the PR
2. Check CI passes
3. Merge

---

## 🔧 Configuration Files

| File                                 | Purpose            |
| ------------------------------------ | ------------------ |
| `.github/workflows/android-ci.yml`   | Main CI pipeline   |
| `.github/workflows/security.yml`     | Security scanning  |
| `.github/workflows/code-quality.yml` | Code quality       |
| `.github/workflows/release.yml`      | Release automation |
| `.github/dependabot.yml`             | Dependency updates |
| `.github/hooks/*`                    | Local Git hooks    |

---

## 📊 CI Status Badges

Add to your README.md:

```markdown
![Android CI](https://github.com/patlar104/novachat/actions/workflows/android-ci.yml/badge.svg)
![Security](https://github.com/patlar104/novachat/actions/workflows/security.yml/badge.svg)
![Code Quality](https://github.com/patlar104/novachat/actions/workflows/code-quality.yml/badge.svg)
```

---

## 🐛 Troubleshooting

### "Hook not running"

```bash
# Make hooks executable
chmod +x .github/hooks/*

# Re-run setup
./.github/hooks/setup-hooks.sh
```

### "CI failing locally passing"

Ensure you're using JDK 21:

```bash
java -version
# Should show version 21
```

### "Tests too slow"

Configure Gradle daemon:

```bash
# In ~/.gradle/gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

---

## 🎯 Best Practices

✅ **DO**:

- Let hooks run (don't use `--no-verify`)
- Write good commit messages
- Fix CI failures promptly
- Review Dependabot PRs
- Run `.github/hooks/setup-hooks.sh` after cloning

❌ **DON'T**:

- Commit secrets or API keys
- Ignore CI failures
- Force push to protected branches
- Skip tests with `--no-verify` regularly
- Commit without testing locally

---

## 📚 More Information

- **Full Documentation**: [CI_CD_SETUP.md](CI_CD_SETUP.md)
- **Git Hooks Details**: [.github/hooks/README.md](../.github/hooks/README.md)
- **GitHub Actions Docs**: https://docs.github.com/actions
- **Conventional Commits**: https://www.conventionalcommits.org/

---

## 💡 Tips

1. **Fast Feedback**: Hooks give instant feedback before CI
2. **Parallel Work**: CI runs multiple jobs in parallel
3. **Caching**: Gradle cache speeds up builds significantly
4. **Automation**: Dependabot keeps you up to date
5. **Security**: CodeQL and secret scanning protect your code

---

## 🆘 Need Help?

1. Check [CI_CD_SETUP.md](CI_CD_SETUP.md) for detailed info
2. View workflow logs in the Actions tab
3. Check hook scripts in `.github/hooks/`
4. Review workflow files in `.github/workflows/`

---

**Ready to go! Start developing and let the CI/CD system handle the rest! 🚀**
